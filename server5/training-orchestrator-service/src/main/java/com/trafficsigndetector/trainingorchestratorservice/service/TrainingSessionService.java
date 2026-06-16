package com.trafficsigndetector.trainingorchestratorservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trafficsigndetector.sharedmodel.Mau;
import com.trafficsigndetector.sharedmodel.MauHL;
import com.trafficsigndetector.sharedmodel.MoHinh;
import com.trafficsigndetector.sharedmodel.PhienBan;
import com.trafficsigndetector.sharedmodel.ThongTinHL;
import com.trafficsigndetector.trainingorchestratorservice.messaging.TrainingJobMessage;
import com.trafficsigndetector.trainingorchestratorservice.messaging.TrainingJobPublisher;
import com.trafficsigndetector.trainingorchestratorservice.messaging.TrainingStatusMessage;
import com.trafficsigndetector.trainingorchestratorservice.messaging.TrainingStatusTracker;
import com.trafficsigndetector.trainingorchestratorservice.persistence.entity.ThongTinHLEntity;
import com.trafficsigndetector.trainingorchestratorservice.persistence.entity.TrainingStatusEventEntity;
import com.trafficsigndetector.trainingorchestratorservice.persistence.jdbc.MauHLJdbcRepository;
import com.trafficsigndetector.trainingorchestratorservice.persistence.jdbc.TrainingSessionJdbcRepository;
import com.trafficsigndetector.trainingorchestratorservice.persistence.jdbc.TrainingStatusEventJdbcRepository;
import com.trafficsigndetector.trainingorchestratorservice.service.state.TrainingLifecycleState;
import com.trafficsigndetector.trainingorchestratorservice.service.state.TrainingSessionStateMachine;
import com.trafficsigndetector.trainingorchestratorservice.service.strategy.TrainingJobBuildStrategy;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class TrainingSessionService {

    private static final TypeReference<List<MauHL>> MAU_HL_LIST_TYPE = new TypeReference<>() {};
    private static final DateTimeFormatter VERSION_SUFFIX_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

    private final TrainingJobPublisher trainingJobPublisher;
    private final TrainingStatusTracker trainingStatusTracker;
    private final TrainingSessionJdbcRepository trainingSessionJdbcRepository;
    private final MauHLJdbcRepository mauHLJdbcRepository;
    private final TrainingStatusEventJdbcRepository trainingStatusEventJdbcRepository;
    private final TrainingSessionStateMachine trainingSessionStateMachine;
    private final List<TrainingJobBuildStrategy> trainingJobBuildStrategies;
    private final ObjectMapper objectMapper;
    private final RestClient aimodelRestClient;
    private final ZoneId versionNameZoneId;
    private final Path workingRoot;
    private final Path workerTempModelDir;
    private final Path aimodelModelStoreDir;
    private final String aimodelModelPublicPrefix;
    private final int maxConcurrentTraining;
    private final int maxQueuedTraining;

    public TrainingSessionService(
            TrainingJobPublisher trainingJobPublisher,
            TrainingStatusTracker trainingStatusTracker,
            TrainingSessionJdbcRepository trainingSessionJdbcRepository,
            MauHLJdbcRepository mauHLJdbcRepository,
            TrainingStatusEventJdbcRepository trainingStatusEventJdbcRepository,
            TrainingSessionStateMachine trainingSessionStateMachine,
            List<TrainingJobBuildStrategy> trainingJobBuildStrategies,
            ObjectMapper objectMapper,
            @Value("${services.aimodel-base-url:http://localhost:8082}") String aimodelBaseUrl,
            @Value("${app.training.version-name.timezone:Asia/Ho_Chi_Minh}") String versionNameTimezone,
            @Value("${app.training.artifact.temp-model-dir:../ai-training-service/runtime/training/outputs}") String workerTempModelDir,
            @Value("${app.training.artifact.aimodel-model-dir:../aimodel-service/models}") String aimodelModelStoreDir,
            @Value("${app.training.artifact.aimodel-public-prefix:/models}") String aimodelModelPublicPrefix,
            @Value("${app.training.admission.max-concurrent:1}") int maxConcurrentTraining,
            @Value("${app.training.admission.max-queued:10}") int maxQueuedTraining
    ) {
        this.trainingJobPublisher = trainingJobPublisher;
        this.trainingStatusTracker = trainingStatusTracker;
        this.trainingSessionJdbcRepository = trainingSessionJdbcRepository;
        this.mauHLJdbcRepository = mauHLJdbcRepository;
        this.trainingStatusEventJdbcRepository = trainingStatusEventJdbcRepository;
        this.trainingSessionStateMachine = trainingSessionStateMachine;
        this.trainingJobBuildStrategies = trainingJobBuildStrategies;
        this.objectMapper = objectMapper;
        this.aimodelRestClient = RestClient.builder().baseUrl(aimodelBaseUrl).build();
        this.versionNameZoneId = ZoneId.of(versionNameTimezone);
        this.workingRoot = Path.of("").toAbsolutePath().normalize();
        this.workerTempModelDir = resolvePath(workerTempModelDir);
        this.aimodelModelStoreDir = resolvePath(aimodelModelStoreDir);
        this.aimodelModelPublicPrefix = aimodelModelPublicPrefix;
        this.maxConcurrentTraining = maxConcurrentTraining;
        this.maxQueuedTraining = maxQueuedTraining;
    }

    @Transactional
    public ThongTinHL createSession(ThongTinHL payload) {
        ThongTinHLEntity session = new ThongTinHLEntity();
        session.setEpochs(readInt(payload == null ? null : payload.epochs(), 20));
        session.setBatchSize(readInt(payload == null ? null : payload.batchSize(), 32));
        session.setLearningRate(readDouble(payload == null ? null : payload.learningRate(), 0.01));
        session.setKichThuocAnh(readInt(payload == null ? null : payload.kichThuocAnh(), 416));
        session.setLoaiThietBi(readString(payload == null ? null : payload.loaiThietBi(), "cpu"));
        session.setEarlyStoppingPatience(readInt(payload == null ? null : payload.earlyStoppingPatience(), 5));
        session.setOptimizer(readString(payload == null ? null : payload.optimizer(), "Adam"));
        session.setTrangThai(TrainingLifecycleState.CREATED.code());

        MoHinh moHinhHL = normalizeModel(payload == null ? null : payload.moHinhHL());
        session.setMoHinhHLJson(writeJson(moHinhHL));

        PhienBan phienBanHL = normalizeVersion(payload == null ? null : payload.phienBanHL());
        session.setPhienBanHLJson(writeJson(phienBanHL));

        List<MauHL> dsMauHL = normalizeSamples(payload == null ? null : payload.dsMauHL());
        if (dsMauHL.isEmpty()) {
            throw new IllegalStateException("Training payload has no samples (DsMauHL). Please select at least one sample.");
        }
        // Keep legacy JSON column for backward compatibility while persisting normalized rows.
        session.setDsMauHLJson(writeJson(dsMauHL));

        // Persist once with a temporary non-null tracking id to satisfy DB constraints,
        // then rewrite it to id-based tracking id for client compatibility.
        session.setTrackingId("pending-" + UUID.randomUUID());
        session = trainingSessionJdbcRepository.save(session);
        replaceMauHL(session, dsMauHL);
        session.setTrackingId(String.valueOf(session.getId()));
        session = trainingSessionJdbcRepository.save(session);

        return toResponse(session);
    }

    @Transactional
    public void startTraining(int thongTinHLId) {
        ThongTinHLEntity session = requireSession(thongTinHLId);
        if (isActiveStatus(session.getTrangThai())) {
            throw new IllegalStateException("Training is already queued or running");
        }
        enforceAdmissionCapacity();

        session.setTrangThai(TrainingLifecycleState.QUEUED.code());
        trainingSessionJdbcRepository.save(session);

        TrainingStatusMessage queued = new TrainingStatusMessage(
                session.getTrackingId(),
            TrainingLifecycleState.QUEUED.code(),
                "Training request queued",
                Instant.now()
        );
        appendStatusEvent(session, queued);
        trainingStatusTracker.onStatus(queued);

        MoHinh moHinh = readJsonModel(session.getMoHinhHLJson());
        PhienBan phienBan = readJsonVersion(session.getPhienBanHLJson());
        List<MauHL> dsMau = readDsMauAsMauHL(session);
        if (dsMau.isEmpty()) {
            throw new IllegalStateException("Training session has no samples. Recreate the session with selected samples.");
        }

        String modelCode = readString(moHinh.ten(), "traffic-sign-default");
        String modelPath = readString(phienBan.duongDanMH(), "/models/yolov8s.pt");
        TrainingJobMessage job = resolveTrainingJobBuildStrategy(session)
                .build(session, modelCode, modelPath, dsMau);
        try {
            trainingJobPublisher.publish(job);
        } catch (AmqpException ex) {
            throw new QueueCapacityExceededException("Training queue is full. Please try again later.");
        }
    }

    private void enforceAdmissionCapacity() {
        long running = trainingSessionJdbcRepository.countByTrangThaiIn(List.of("RUNNING", "PREPARING_DATASET"));
        long queued = trainingSessionJdbcRepository.countByTrangThai("QUEUED");

        if (running > maxConcurrentTraining) {
            throw new QueueCapacityExceededException("Training workers are saturated. Please try again later.");
        }

        if (queued >= maxQueuedTraining) {
            throw new QueueCapacityExceededException("Training queue is full. Please try again later.");
        }
    }

    @Transactional(readOnly = true)
    public ThongTinHL getResult(int thongTinHLId) {
        return toResponse(requireSession(thongTinHLId));
    }

    @Transactional
    public int saveVersion(int thongTinHLId) {
        ThongTinHLEntity session = requireSession(thongTinHLId);
        if (!"COMPLETED".equalsIgnoreCase(session.getTrangThai())) {
            throw new IllegalStateException("Training is not completed yet");
        }

        MoHinh moHinh = readJsonModel(session.getMoHinhHLJson());
        int moHinhId = readInt(moHinh.id(), 1);
        String tenVersion = "v" + thongTinHLId + " " + LocalDateTime.now(versionNameZoneId).format(VERSION_SUFFIX_FORMATTER);

        Path tempModelPath = resolveTempModelPath(session.getDuongDanMoHinhKetQua());
        String targetFileName = buildTargetFileName(tenVersion, tempModelPath.getFileName().toString());
        Path aimodelTargetPath = aimodelModelStoreDir.resolve("mo-hinh-" + moHinhId).resolve(targetFileName).normalize();
        copyToAimodelStore(tempModelPath, aimodelTargetPath);
        String modelPublicPath = normalizeAimodelPublicPath(moHinhId, targetFileName);

        PhienBan payload = new PhienBan(
                null,
                tenVersion,
                "Version generated from training job " + thongTinHLId,
                modelPublicPath
        );

        PhienBan created;
        try {
            created = aimodelRestClient.post()
                    .uri("/api/mo-hinh/{moHinhId}/phien-ban", moHinhId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(PhienBan.class);
        } catch (RuntimeException ex) {
            deleteIfExistsQuietly(aimodelTargetPath);
            throw ex;
        }

        if (created == null || created.id() == null) {
            deleteIfExistsQuietly(aimodelTargetPath);
            throw new IllegalStateException("Could not create model version");
        }

        deleteTempArtifact(tempModelPath);

        session.setPhienBanHLJson(writeJson(created));
        session.setDuongDanMoHinhKetQua(modelPublicPath);
        trainingSessionJdbcRepository.save(session);

        return created.id();
    }

    @Transactional
    public void onStatus(TrainingStatusMessage message) {
        if (message.trackingId() == null || message.trackingId().isBlank()) {
            return;
        }

        Optional<ThongTinHLEntity> sessionOptional = trainingSessionJdbcRepository.findByTrackingId(message.trackingId());
        if (sessionOptional.isEmpty()) {
            return;
        }
        ThongTinHLEntity session = sessionOptional.get();

        if (message.currentEpoch() != null) {
            session.setCurrentEpoch(message.currentEpoch());
        }
        if (message.precision() != null) {
            session.setDoChinhXac(message.precision());
        }
        if (message.recall() != null) {
            session.setDoNhay(message.recall());
        }
        if (message.modelArtifactPath() != null && !message.modelArtifactPath().isBlank()) {
            session.setDuongDanMoHinhKetQua(message.modelArtifactPath());
        }

        trainingSessionStateMachine.apply(session, message);

        trainingSessionJdbcRepository.save(session);
        appendStatusEvent(session, message);
    }

    @Transactional(readOnly = true)
    public Optional<TrainingStatusMessage> getLatestStatus(String trackingId) {
        return trainingStatusEventJdbcRepository.findTopByTrackingIdOrderByUpdatedAtDescIdDesc(trackingId)
                .map(this::toStatusMessage);
    }

    @Transactional(readOnly = true)
    public List<TrainingStatusMessage> getTimeline(String trackingId) {
        List<TrainingStatusEventEntity> events = trainingStatusEventJdbcRepository.findByTrackingIdOrderByUpdatedAtAscIdAsc(trackingId);
        List<TrainingStatusMessage> result = new ArrayList<>();
        for (TrainingStatusEventEntity event : events) {
            result.add(toStatusMessage(event));
        }
        return result;
    }

    private ThongTinHLEntity requireSession(int id) {
        return trainingSessionJdbcRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Training session not found: " + id));
    }

    private void appendStatusEvent(ThongTinHLEntity session, TrainingStatusMessage message) {
        TrainingStatusEventEntity event = new TrainingStatusEventEntity();
        event.setSessionId(session.getId());
        event.setTrackingId(session.getTrackingId());
        event.setState(message.state() == null ? "UNKNOWN" : message.state());
        event.setDetail(message.detail());
        event.setUpdatedAt(message.updatedAt() == null ? Instant.now() : message.updatedAt());
        event.setCurrentEpoch(message.currentEpoch());
        event.setPrecision(message.precision());
        event.setRecall(message.recall());
        event.setLogLine(message.logLine());
        event.setModelArtifactPath(message.modelArtifactPath());
        event.setCreatedAt(Instant.now());
        trainingStatusEventJdbcRepository.save(event);
    }

    private TrainingStatusMessage toStatusMessage(TrainingStatusEventEntity event) {
        return new TrainingStatusMessage(
                event.getTrackingId(),
                event.getState(),
                event.getDetail(),
                event.getUpdatedAt(),
                event.getCurrentEpoch(),
                event.getPrecision(),
                event.getRecall(),
                event.getLogLine(),
                event.getModelArtifactPath()
        );
    }

    private ThongTinHL toResponse(ThongTinHLEntity session) {
        MoHinh mo = readJsonModel(session.getMoHinhHLJson());
        PhienBan pb = readJsonVersion(session.getPhienBanHLJson());
        List<MauHL> ds = readDsMauAsMauHL(session);
        return new ThongTinHL(
                session.getId(),
                session.getTrackingId(),
                session.getEpochs(),
                session.getBatchSize(),
                session.getTrangThai(),
                session.getBatDauLuc(),
                session.getKetThucLuc(),
                session.getDoChinhXac() == null ? 0.0 : session.getDoChinhXac(),
                session.getDoNhay() == null ? 0.0 : session.getDoNhay(),
                session.getCurrentEpoch(),
                session.getLearningRate(),
                session.getKichThuocAnh(),
                session.getLoaiThietBi(),
                session.getEarlyStoppingPatience(),
                session.getOptimizer(),
                mo,
                pb,
                ds,
                session.getDuongDanMoHinhKetQua()
        );
    }

    private void replaceMauHL(ThongTinHLEntity session, List<MauHL> samples) {
        mauHLJdbcRepository.deleteByThongTinHLId(session.getId());
        List<String> jsonLines = new ArrayList<>();
        for (MauHL m : samples) {
            jsonLines.add(writeJson(m));
        }
        mauHLJdbcRepository.insertRows(session.getId(), jsonLines);
    }

    private List<MauHL> readDsMauAsMauHL(ThongTinHLEntity session) {
        List<String> rows = mauHLJdbcRepository.findThongTinMauJsonOrdered(session.getId());
        if (!rows.isEmpty()) {
            List<MauHL> result = new ArrayList<>();
            for (String json : rows) {
                try {
                    result.add(objectMapper.readValue(json, MauHL.class));
                } catch (JsonProcessingException ex) {
                    throw new IllegalStateException("Invalid MauHL JSON row", ex);
                }
            }
            return result;
        }
        try {
            if (session.getDsMauHLJson() != null && !session.getDsMauHLJson().isBlank()) {
                return objectMapper.readValue(session.getDsMauHLJson(), MAU_HL_LIST_TYPE);
            }
        } catch (JsonProcessingException ex) {
            return List.of();
        }
        return List.of();
    }

    private MoHinh normalizeModel(MoHinh raw) {
        if (raw == null) {
            return new MoHinh(1, "TrafficSign-YOLO", "yolov8s.pt", List.of());
        }
        Integer id = raw.id() != null ? raw.id() : 1;
        String ten = raw.ten() != null && !raw.ten().isBlank() ? raw.ten() : "TrafficSign-YOLO";
        String goc = raw.moHinhGoc() != null ? raw.moHinhGoc() : "yolov8s.pt";
        return new MoHinh(id, ten, goc, raw.dsPhienBan());
    }

    private PhienBan normalizeVersion(PhienBan raw) {
        if (raw == null) {
            return new PhienBan(101, "v1.0", "Default version", "/models/yolov8s.pt");
        }
        Integer id = raw.id() != null ? raw.id() : 101;
        String ten = raw.ten() != null && !raw.ten().isBlank() ? raw.ten() : "v1.0";
        String moTa = raw.moTa() != null ? raw.moTa() : "Default version";
        String path = raw.duongDanMH() != null ? raw.duongDanMH() : "/models/yolov8s.pt";
        return new PhienBan(id, ten, moTa, path);
    }

    private List<MauHL> normalizeSamples(List<MauHL> raw) {
        if (raw == null) {
            return List.of();
        }
        List<MauHL> out = new ArrayList<>();
        for (MauHL item : raw) {
            if (item != null) {
                out.add(item);
            }
        }
        return out;
    }

    private MoHinh readJsonModel(String json) {
        if (json == null || json.isBlank()) {
            return normalizeModel(null);
        }
        try {
            return normalizeModel(objectMapper.readValue(json, MoHinh.class));
        } catch (JsonProcessingException ex) {
            return normalizeModel(null);
        }
    }

    private PhienBan readJsonVersion(String json) {
        if (json == null || json.isBlank()) {
            return normalizeVersion(null);
        }
        try {
            return normalizeVersion(objectMapper.readValue(json, PhienBan.class));
        } catch (JsonProcessingException ex) {
            return normalizeVersion(null);
        }
    }

    private int readInt(Integer raw, int fallback) {
        return raw == null ? fallback : raw;
    }

    private double readDouble(Double raw, double fallback) {
        return raw == null ? fallback : raw;
    }

    private String readString(String raw, String fallback) {
        if (raw == null) {
            return fallback;
        }
        String t = raw.trim();
        return t.isEmpty() ? fallback : t;
    }

    private boolean isActiveStatus(String rawState) {
        TrainingLifecycleState state = TrainingLifecycleState.fromExternal(rawState);
        return Set.of(
                TrainingLifecycleState.QUEUED,
                TrainingLifecycleState.RUNNING,
                TrainingLifecycleState.PREPARING_DATASET
        ).contains(state);
    }

    private TrainingJobBuildStrategy resolveTrainingJobBuildStrategy(ThongTinHLEntity session) {
        for (TrainingJobBuildStrategy strategy : trainingJobBuildStrategies) {
            if (strategy.supports(session)) {
                return strategy;
            }
        }
        throw new IllegalStateException("No TrainingJobBuildStrategy supports this training session");
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not serialize training payload", ex);
        }
    }

    private Path resolveTempModelPath(String artifactPath) {
        String normalized = artifactPath == null ? "" : artifactPath.trim();
        if (normalized.isEmpty()) {
            throw new IllegalStateException("Training result has no temporary model artifact path");
        }

        Path absolute = toAbsolutePath(normalized);
        if (absolute != null && Files.exists(absolute)) {
            return absolute;
        }

        String slashNormalized = normalized.replace('\\', '/');
        String fileName = extractFileName(slashNormalized);

        Path candidateFromKnownDirs = resolveTempArtifactFromKnownDirs(fileName);
        if (candidateFromKnownDirs != null) {
            return candidateFromKnownDirs;
        }

        if (slashNormalized.startsWith("/runtime/training/outputs/")
                || slashNormalized.startsWith("runtime/training/outputs/")
                || slashNormalized.startsWith("/models/outputs/")
                || slashNormalized.startsWith("models/outputs/")
                || !slashNormalized.contains("/")) {
            Path candidate = workerTempModelDir.resolve(fileName).normalize();
            if (Files.exists(candidate)) {
                return candidate;
            }
        }

        String localRelative = slashNormalized.startsWith("/") ? slashNormalized.substring(1) : slashNormalized;

        Path relative = workingRoot.resolve(localRelative).normalize();
        if (Files.exists(relative)) {
            return relative;
        }

        Path moduleRelative = workingRoot.resolve("training-orchestrator-service").resolve(localRelative).normalize();
        if (Files.exists(moduleRelative)) {
            return moduleRelative;
        }

        Path server5ModuleRelative = workingRoot.resolve("server5")
                .resolve("training-orchestrator-service")
                .resolve(localRelative)
                .normalize();
        if (Files.exists(server5ModuleRelative)) {
            return server5ModuleRelative;
        }

        throw new IllegalStateException("Temporary model artifact not found: " + artifactPath);
    }

    private Path resolveTempArtifactFromKnownDirs(String fileName) {
        List<Path> candidateDirs = List.of(
                workerTempModelDir,
                workingRoot.resolve("runtime").resolve("training").resolve("outputs"),
                workingRoot.resolve("server5").resolve("runtime").resolve("training").resolve("outputs"),
                workingRoot.resolve("ai-training-service").resolve("runtime").resolve("training").resolve("outputs"),
                workingRoot.resolve("server5").resolve("ai-training-service").resolve("runtime").resolve("training").resolve("outputs"),
                workingRoot.resolve("ai-training-service").resolve("models").resolve("outputs"),
                workingRoot.resolve("server5").resolve("ai-training-service").resolve("models").resolve("outputs")
        );

        for (Path dir : candidateDirs) {
            Path candidate = dir.resolve(fileName).normalize();
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private String buildTargetFileName(String versionName, String sourceFileName) {
        String extension = ".pt";
        int dotIndex = sourceFileName.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < sourceFileName.length() - 1) {
            extension = sourceFileName.substring(dotIndex);
        }

        String safeBase = versionName == null ? "version" : versionName.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (safeBase.isBlank()) {
            safeBase = "version";
        }
        if (safeBase.length() > 80) {
            safeBase = safeBase.substring(0, 80);
        }

        return safeBase + extension;
    }

    private String normalizeAimodelPublicPath(int moHinhId, String fileName) {
        String prefix = aimodelModelPublicPrefix.endsWith("/")
                ? aimodelModelPublicPrefix.substring(0, aimodelModelPublicPrefix.length() - 1)
                : aimodelModelPublicPrefix;
        return prefix + "/mo-hinh-" + moHinhId + "/" + fileName;
    }

    private void copyToAimodelStore(Path source, Path destination) {
        try {
            Files.createDirectories(destination.getParent());
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not copy temporary model artifact to aimodel-service storage", ex);
        }
    }

    private void deleteTempArtifact(Path tempModelPath) {
        try {
            Files.deleteIfExists(tempModelPath);
        } catch (IOException ex) {
            throw new IllegalStateException("Saved version but could not delete temporary model artifact: " + tempModelPath, ex);
        }
    }

    private void deleteIfExistsQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Best-effort cleanup only.
        }
    }

    private Path resolvePath(String raw) {
        Path path = Path.of(raw);
        if (path.isAbsolute()) {
            return path.normalize();
        }

        Path direct = workingRoot.resolve(path).normalize();
        Path moduleRelative = workingRoot.resolve("training-orchestrator-service").resolve(path).normalize();
        Path server5Direct = workingRoot.resolve("server5").resolve(path).normalize();
        Path server5ModuleRelative = workingRoot.resolve("server5")
                .resolve("training-orchestrator-service")
                .resolve(path)
                .normalize();

        if (Files.exists(direct)) {
            return direct;
        }
        if (Files.exists(moduleRelative)) {
            return moduleRelative;
        }
        if (Files.exists(server5Direct)) {
            return server5Direct;
        }
        if (Files.exists(server5ModuleRelative)) {
            return server5ModuleRelative;
        }
        return direct;
    }

    private Path toAbsolutePath(String raw) {
        try {
            Path path = Path.of(raw);
            return path.isAbsolute() ? path.normalize() : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private String extractFileName(String input) {
        int slash = input.lastIndexOf('/');
        return slash >= 0 ? input.substring(slash + 1) : input;
    }
}

