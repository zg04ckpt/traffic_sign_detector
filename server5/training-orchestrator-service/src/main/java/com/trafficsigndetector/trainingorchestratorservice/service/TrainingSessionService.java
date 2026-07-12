package com.trafficsigndetector.trainingorchestratorservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trafficsigndetector.sharedmodel.MauHL;
import com.trafficsigndetector.sharedmodel.MoHinh;
import com.trafficsigndetector.sharedmodel.PhienBan;
import com.trafficsigndetector.sharedmodel.ThongTinHL;
import com.trafficsigndetector.trainingorchestratorservice.constant.AppConstants;
import com.trafficsigndetector.trainingorchestratorservice.messaging.TrainingStatusMessage;
import com.trafficsigndetector.trainingorchestratorservice.messaging.TrainingStatusTracker;
import com.trafficsigndetector.trainingorchestratorservice.persistence.entity.ThongTinHLEntity;
import com.trafficsigndetector.trainingorchestratorservice.persistence.entity.TrainingStatusEventEntity;
import com.trafficsigndetector.trainingorchestratorservice.persistence.jdbc.MauHLJdbcRepository;
import com.trafficsigndetector.trainingorchestratorservice.persistence.jdbc.TrainingSessionJdbcRepository;
import com.trafficsigndetector.trainingorchestratorservice.persistence.jdbc.TrainingStatusEventJdbcRepository;
import com.trafficsigndetector.trainingorchestratorservice.service.state.TrainingLifecycleState;
import com.trafficsigndetector.trainingorchestratorservice.service.state.TrainingSessionStateMachine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.nio.file.Path;
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

    private final TrainingStatusTracker trainingStatusTracker;
    private final TrainingSessionJdbcRepository trainingSessionJdbcRepository;
    private final MauHLJdbcRepository mauHLJdbcRepository;
    private final TrainingStatusEventJdbcRepository trainingStatusEventJdbcRepository;
    private final TrainingSessionStateMachine trainingSessionStateMachine;
    private final ObjectMapper objectMapper;
    private final RestClient aimodelRestClient;
    private final ZoneId versionNameZoneId;

    private final TrainingJobScheduler trainingJobScheduler;
    private final TrainingFileStorageService fileStorageService;

    public TrainingSessionService(
            TrainingStatusTracker trainingStatusTracker,
            TrainingSessionJdbcRepository trainingSessionJdbcRepository,
            MauHLJdbcRepository mauHLJdbcRepository,
            TrainingStatusEventJdbcRepository trainingStatusEventJdbcRepository,
            TrainingSessionStateMachine trainingSessionStateMachine,
            ObjectMapper objectMapper,
            TrainingJobScheduler trainingJobScheduler,
            TrainingFileStorageService fileStorageService,
            @Value("${services.aimodel-base-url:http://localhost:8082}") String aimodelBaseUrl,
            @Value("${app.training.version-name.timezone:Asia/Ho_Chi_Minh}") String versionNameTimezone
    ) {
        this.trainingStatusTracker = trainingStatusTracker;
        this.trainingSessionJdbcRepository = trainingSessionJdbcRepository;
        this.mauHLJdbcRepository = mauHLJdbcRepository;
        this.trainingStatusEventJdbcRepository = trainingStatusEventJdbcRepository;
        this.trainingSessionStateMachine = trainingSessionStateMachine;
        this.objectMapper = objectMapper;
        this.trainingJobScheduler = trainingJobScheduler;
        this.fileStorageService = fileStorageService;
        this.aimodelRestClient = RestClient.builder().baseUrl(aimodelBaseUrl).build();
        this.versionNameZoneId = ZoneId.of(versionNameTimezone);
    }

    @Transactional
    public ThongTinHL createSession(ThongTinHL payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Payload is required");
        }
        ThongTinHLEntity session = new ThongTinHLEntity();
        session.setEpochs(requireParamInt(payload.epochs(), "epochs"));
        session.setBatchSize(requireParamInt(payload.batchSize(), "batchSize"));
        session.setLearningRate(requireParamDouble(payload.learningRate(), "learningRate"));
        session.setKichThuocAnh(requireParamInt(payload.kichThuocAnh(), "kichThuocAnh"));
        session.setLoaiThietBi(requireParamString(payload.loaiThietBi(), "loaiThietBi"));
        session.setEarlyStoppingPatience(requireParamInt(payload.earlyStoppingPatience(), "earlyStoppingPatience"));
        session.setOptimizer(requireParamString(payload.optimizer(), "optimizer"));
        session.setTrangThai(AppConstants.STATE_CREATED);

        MoHinh moHinhHL = normalizeModel(payload.moHinhHL());
        session.setMoHinhHLJson(writeJson(moHinhHL));

        PhienBan phienBanHL = normalizeVersion(payload.phienBanHL());
        session.setPhienBanHLJson(writeJson(phienBanHL));

        List<MauHL> dsMauHL = normalizeSamples(payload.dsMauHL());
        if (dsMauHL.isEmpty()) {
            throw new IllegalStateException("Training payload has no samples (DsMauHL). Please select at least one sample.");
        }
        session.setDsMauHLJson(writeJson(dsMauHL));

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
        trainingJobScheduler.enforceAdmissionCapacity();

        session.setTrangThai(AppConstants.STATE_QUEUED);
        trainingSessionJdbcRepository.save(session);

        TrainingStatusMessage queued = new TrainingStatusMessage(
                session.getTrackingId(),
                AppConstants.STATE_QUEUED,
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

        String modelCode = requireParamString(moHinh.ten(), "moHinh.ten");
        String modelPath = requireParamString(phienBan.duongDanMH(), "phienBan.duongDanMH");
        
        trainingJobScheduler.scheduleJob(session, modelCode, modelPath, dsMau);
    }

    @Transactional(readOnly = true)
    public ThongTinHL getResult(int thongTinHLId) {
        return toResponse(requireSession(thongTinHLId));
    }

    public int saveVersion(int thongTinHLId) {
        // Step 1: Validate session locally
        ThongTinHLEntity session = requireSession(thongTinHLId);
        if (!AppConstants.STATE_COMPLETED.equalsIgnoreCase(session.getTrangThai())) {
            throw new IllegalStateException("Training is not completed yet");
        }

        MoHinh moHinh = readJsonModel(session.getMoHinhHLJson());
        int moHinhId = requireParamInt(moHinh.id(), "moHinh.id");
        String tenVersion = "v" + thongTinHLId + " " + LocalDateTime.now(versionNameZoneId).format(VERSION_SUFFIX_FORMATTER);

        Path tempModelPath = fileStorageService.resolveTempModelPath(session.getDuongDanMoHinhKetQua());
        String targetFileName = fileStorageService.buildTargetFileName(tenVersion, tempModelPath.getFileName().toString());
        
        Path aimodelTargetPath = fileStorageService.copyToAimodelStore(tempModelPath, moHinhId, targetFileName);
        String modelPublicPath = fileStorageService.normalizeAimodelPublicPath(moHinhId, targetFileName);

        PhienBan payload = new PhienBan(
                null,
                tenVersion,
                "Version generated from training job " + thongTinHLId,
                modelPublicPath
        );

        // Step 2: Make external REST call without holding the DB transaction
        PhienBan created;
        try {
            created = aimodelRestClient.post()
                    .uri("/api/mo-hinh/{moHinhId}/phien-ban", moHinhId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(PhienBan.class);
        } catch (RuntimeException ex) {
            fileStorageService.deleteIfExistsQuietly(aimodelTargetPath);
            throw ex;
        }

        if (created == null || created.id() == null) {
            fileStorageService.deleteIfExistsQuietly(aimodelTargetPath);
            throw new IllegalStateException("Could not create model version via aimodel-service");
        }

        fileStorageService.deleteTempArtifact(tempModelPath);

        // Step 3: Update local DB in its own new transaction
        updateSessionAfterVersionCreation(session.getId(), created, modelPublicPath);

        return created.id();
    }

    @Transactional
    public void updateSessionAfterVersionCreation(int sessionId, PhienBan created, String modelPublicPath) {
        ThongTinHLEntity session = requireSession(sessionId);
        session.setPhienBanHLJson(writeJson(created));
        session.setDuongDanMoHinhKetQua(modelPublicPath);
        trainingSessionJdbcRepository.save(session);
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
            throw new IllegalArgumentException("MoHinhHL is required");
        }
        return new MoHinh(
            requireParamInt(raw.id(), "moHinhHL.id"), 
            requireParamString(raw.ten(), "moHinhHL.ten"), 
            requireParamString(raw.moHinhGoc(), "moHinhHL.moHinhGoc"), 
            raw.dsPhienBan()
        );
    }

    private PhienBan normalizeVersion(PhienBan raw) {
        if (raw == null) {
            throw new IllegalArgumentException("PhienBanHL is required");
        }
        return new PhienBan(
            requireParamInt(raw.id(), "phienBanHL.id"), 
            requireParamString(raw.ten(), "phienBanHL.ten"), 
            requireParamString(raw.moTa(), "phienBanHL.moTa"), 
            requireParamString(raw.duongDanMH(), "phienBanHL.duongDanMH")
        );
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
            throw new IllegalStateException("Corrupted DB: MoHinhHLJson is blank");
        }
        try {
            return objectMapper.readValue(json, MoHinh.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Corrupted DB: MoHinhHLJson invalid", ex);
        }
    }

    private PhienBan readJsonVersion(String json) {
        if (json == null || json.isBlank()) {
             throw new IllegalStateException("Corrupted DB: PhienBanHLJson is blank");
        }
        try {
            return objectMapper.readValue(json, PhienBan.class);
        } catch (JsonProcessingException ex) {
             throw new IllegalStateException("Corrupted DB: PhienBanHLJson invalid", ex);
        }
    }

    private int requireParamInt(Integer raw, String paramName) {
        if (raw == null) {
            throw new IllegalArgumentException("Missing required parameter: " + paramName);
        }
        return raw;
    }

    private double requireParamDouble(Double raw, String paramName) {
        if (raw == null) {
            throw new IllegalArgumentException("Missing required parameter: " + paramName);
        }
        return raw;
    }

    private String requireParamString(String raw, String paramName) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required parameter: " + paramName);
        }
        return raw.trim();
    }

    private boolean isActiveStatus(String rawState) {
        TrainingLifecycleState state = TrainingLifecycleState.fromExternal(rawState);
        return Set.of(
                TrainingLifecycleState.QUEUED,
                TrainingLifecycleState.RUNNING,
                TrainingLifecycleState.PREPARING_DATASET
        ).contains(state);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not serialize training payload", ex);
        }
    }
}
