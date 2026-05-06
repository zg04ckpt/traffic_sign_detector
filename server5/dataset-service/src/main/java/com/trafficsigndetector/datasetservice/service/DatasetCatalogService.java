package com.trafficsigndetector.datasetservice.service;

import com.trafficsigndetector.datasetservice.repository.DatasetJdbcRepository;
import com.trafficsigndetector.sharedmodel.LoaiBien;
import com.trafficsigndetector.sharedmodel.Mau;
import com.trafficsigndetector.sharedmodel.TapDuLieu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class DatasetCatalogService {

    private static final Logger log = LoggerFactory.getLogger(DatasetCatalogService.class);

    private final DatasetJdbcRepository datasetRepository;
    private final Path uploadRoot;

    public DatasetCatalogService(
            DatasetJdbcRepository datasetRepository,
            @Value("${app.storage.upload-dir:dataset-service/uploads}") String uploadDir
    ) {
        this.datasetRepository = datasetRepository;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        initStorage();
    }

    @Transactional
    public TapDuLieu createDataset(
            String tenDataset,
            List<MultipartFile> imageFiles,
            List<MultipartFile> labelFiles
    ) {
        String datasetName = normalizeDatasetName(tenDataset);
        List<MultipartFile> images = normalizeFiles(imageFiles);
        if (images.isEmpty()) {
            throw new IllegalArgumentException("Danh sách ảnh không được để trống");
        }

        log.info("Creating dataset '{}' with {} images and {} label files", datasetName, images.size(), normalizeFiles(labelFiles).size());

        int datasetId = datasetRepository.insertTapDuLieu(datasetName);

        try {
            addUploadedSamples(datasetId, images, normalizeFiles(labelFiles));
            TapDuLieu saved = datasetRepository.findFullById(datasetId)
                    .orElseThrow(() -> new IllegalStateException("Dataset not found after create: " + datasetId));
            log.info("Created dataset id={} name='{}' samples={}", saved.id(), saved.ten(),
                    saved.dsMau() == null ? 0 : saved.dsMau().size());
            return saved;
        } catch (RuntimeException ex) {
            log.error("Create dataset failed for id={} name='{}'", datasetId, datasetName, ex);
            cleanupDatasetFolder(datasetId);
            datasetRepository.deleteTapDuLieu(datasetId);
            throw ex;
        }
    }

    @Transactional
    public TapDuLieu updateDataset(
            int datasetId,
            String tenDataset,
            List<MultipartFile> addImages,
            List<MultipartFile> labelFiles,
            List<Integer> removeSampleIds
    ) {
        TapDuLieu current = requireDataset(datasetId);

        if (tenDataset != null && !tenDataset.isBlank()) {
            datasetRepository.updateTapDuLieuTen(datasetId, normalizeDatasetName(tenDataset));
        }

        removeSamples(datasetId, current, removeSampleIds);
        addUploadedSamples(datasetId, normalizeFiles(addImages), normalizeFiles(labelFiles));

        return datasetRepository.findFullById(datasetId)
                .orElseThrow(() -> new IllegalStateException("Dataset not found: " + datasetId));
    }

    @Transactional
    public void deleteDataset(int datasetId) {
        requireDataset(datasetId);
        cleanupDatasetFolder(datasetId);
        datasetRepository.deleteTapDuLieu(datasetId);
    }

    @Transactional(readOnly = true)
    public List<TapDuLieu> getAllDatasets() {
        return datasetRepository.findAllFullOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public TapDuLieu getDatasetById(int datasetId) {
        return requireDataset(datasetId);
    }

    @Transactional
    public Mau saveSample(int datasetId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File upload is empty");
        }

        requireDataset(datasetId);

        String originalName = file.getOriginalFilename();
        String extension = extractExtension(originalName);
        String generated = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);

        Path datasetFolder = uploadRoot.resolve("dataset-" + datasetId);
        try {
            Files.createDirectories(datasetFolder);
            Files.copy(file.getInputStream(), datasetFolder.resolve(generated), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Could not store file: " + ex.getMessage());
        }

        String duongDan = "/uploads/dataset-" + datasetId + "/" + generated;
        int mauId = datasetRepository.insertMau(datasetId, duongDan, "unknown");

        return new Mau(mauId, duongDan, "unknown", List.of());
    }

    public String getUploadRoot() {
        return uploadRoot.toString();
    }

    private TapDuLieu requireDataset(int datasetId) {
        return datasetRepository.findFullById(datasetId)
                .orElseThrow(() -> new IllegalStateException("Dataset not found: " + datasetId));
    }

    private void initStorage() {
        try {
            Files.createDirectories(uploadRoot);
            log.info("Dataset upload root: {}", uploadRoot);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot initialize upload directory: " + uploadRoot, ex);
        }
    }

    private String normalizeDatasetName(String tenDataset) {
        if (tenDataset == null || tenDataset.isBlank()) {
            throw new IllegalArgumentException("Tên dataset không được để trống");
        }
        return tenDataset.trim();
    }

    private List<MultipartFile> normalizeFiles(List<MultipartFile> files) {
        if (files == null) {
            return List.of();
        }
        List<MultipartFile> result = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                result.add(file);
            }
        }
        return result;
    }

    private void removeSamples(int datasetId, TapDuLieu current, List<Integer> removeSampleIds) {
        if (removeSampleIds == null || removeSampleIds.isEmpty()) {
            return;
        }
        Set<Integer> removeSet = new HashSet<>(removeSampleIds);
        if (current.dsMau() != null) {
            for (Mau sample : current.dsMau()) {
                if (sample.id() != null && removeSet.contains(sample.id())) {
                    deleteSampleFileIfExists(datasetId, sample.duongDanAnh());
                }
            }
        }
        datasetRepository.deleteMauForDatasetWhereIdIn(datasetId, new ArrayList<>(removeSet));
    }

    private void addUploadedSamples(int datasetId, List<MultipartFile> imageFiles, List<MultipartFile> labelFiles) {
        if (imageFiles.isEmpty()) {
            return;
        }

        List<LabelFileContent> parsedLabels = parseLabelFiles(labelFiles);
        Map<String, List<ParsedLabel>> labelsByImageName = new HashMap<>();
        for (LabelFileContent item : parsedLabels) {
            labelsByImageName.put(item.baseName(), item.labels());
        }
        boolean useIndexFallback = !parsedLabels.isEmpty() && parsedLabels.size() == imageFiles.size();

        Map<Integer, LoaiBien> loaiBienMap = datasetRepository.loadLoaiBienById();

        Path datasetFolder = uploadRoot.resolve("dataset-" + datasetId);
        try {
            Files.createDirectories(datasetFolder);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Không thể tạo thư mục dataset: " + ex.getMessage(), ex);
        }

        for (int i = 0; i < imageFiles.size(); i++) {
            MultipartFile image = imageFiles.get(i);
            String generated = generateImageName(image.getOriginalFilename());
            Path target = datasetFolder.resolve(generated);
            try {
                Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                throw new IllegalArgumentException("Không thể lưu ảnh upload: " + ex.getMessage(), ex);
            }

            String duongDan = "/uploads/dataset-" + datasetId + "/" + generated;
            int mauId = datasetRepository.insertMau(datasetId, duongDan, "unknown");

            String imageBaseName = extractBaseName(image.getOriginalFilename());
            List<ParsedLabel> labels = labelsByImageName.get(imageBaseName);
            if (labels == null && useIndexFallback) {
                labels = parsedLabels.get(i).labels();
            }
            if (labels != null && !labels.isEmpty()) {
                attachLabels(mauId, labels, loaiBienMap, image.getOriginalFilename());
            }
        }
    }

    private void attachLabels(
            int mauId,
            List<ParsedLabel> labels,
            Map<Integer, LoaiBien> loaiBienMap,
            String sourceName
    ) {
        for (ParsedLabel label : labels) {
            LoaiBien loaiBien = loaiBienMap.get(label.classId());
            if (loaiBien == null || loaiBien.id() == null) {
                throw new IllegalArgumentException("Label file " + sourceName + " chứa classId không hợp lệ: " + label.classId());
            }

            datasetRepository.insertKhungNhanDang(
                    mauId,
                    label.xCenter(),
                    label.yCenter(),
                    label.w(),
                    label.h(),
                    loaiBien.id()
            );
        }
    }

    private List<LabelFileContent> parseLabelFiles(List<MultipartFile> labelFiles) {
        if (labelFiles.isEmpty()) {
            return List.of();
        }

        List<LabelFileContent> result = new ArrayList<>();
        for (MultipartFile file : labelFiles) {
            String baseName = extractBaseName(file.getOriginalFilename());
            result.add(new LabelFileContent(baseName, parseLabelContent(file)));
        }
        return result;
    }

    private List<ParsedLabel> parseLabelContent(MultipartFile labelFile) {
        List<ParsedLabel> labels = new ArrayList<>();
        String fileName = labelFile.getOriginalFilename() == null ? "label.txt" : labelFile.getOriginalFilename();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(labelFile.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }

                String[] tokens = trimmed.split("\\s+");
                if (tokens.length != 5) {
                    throw new IllegalArgumentException("Label file " + fileName + " sai định dạng tại dòng " + lineNo);
                }

                try {
                    int classId = Integer.parseInt(tokens[0]);
                    float xCenter = Float.parseFloat(tokens[1]);
                    float yCenter = Float.parseFloat(tokens[2]);
                    float w = Float.parseFloat(tokens[3]);
                    float h = Float.parseFloat(tokens[4]);
                    labels.add(new ParsedLabel(classId, xCenter, yCenter, w, h));
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Label file " + fileName + " chứa số không hợp lệ tại dòng " + lineNo);
                }
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Không thể đọc label file " + fileName + ": " + ex.getMessage(), ex);
        }

        return labels;
    }

    private String generateImageName(String originalName) {
        String extension = extractExtension(originalName);
        return UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
    }

    private String extractBaseName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }
        String normalized = fileName.replace('\\', '/');
        String rawName = normalized.substring(normalized.lastIndexOf('/') + 1);
        int dot = rawName.lastIndexOf('.');
        if (dot <= 0) {
            return rawName;
        }
        return rawName.substring(0, dot);
    }

    private void deleteSampleFileIfExists(int datasetId, String duongDanAnh) {
        if (duongDanAnh == null || duongDanAnh.isBlank()) {
            return;
        }

        String normalized = duongDanAnh.replace('\\', '/');
        String relative;
        if (normalized.startsWith("/uploads/")) {
            relative = normalized.substring("/uploads/".length());
        } else if (normalized.startsWith("uploads/")) {
            relative = normalized.substring("uploads/".length());
        } else {
            String fileName = normalized.substring(normalized.lastIndexOf('/') + 1);
            relative = "dataset-" + datasetId + "/" + fileName;
        }

        Path path = uploadRoot.resolve(relative).normalize();
        if (!path.startsWith(uploadRoot)) {
            return;
        }

        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Best effort cleanup.
        }
    }

    private void cleanupDatasetFolder(Integer datasetId) {
        if (datasetId == null) {
            return;
        }
        Path folder = uploadRoot.resolve("dataset-" + datasetId).normalize();
        if (!folder.startsWith(uploadRoot) || !Files.exists(folder)) {
            return;
        }

        try {
            Files.walk(folder)
                    .sorted((a, b) -> b.getNameCount() - a.getNameCount())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                            // Best effort cleanup.
                        }
                    });
        } catch (IOException ignored) {
            // Best effort cleanup.
        }
    }

    private String extractExtension(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        int idx = name.lastIndexOf('.');
        if (idx < 0 || idx == name.length() - 1) {
            return "";
        }
        return name.substring(idx + 1).replaceAll("[^A-Za-z0-9]", "");
    }

    private record LabelFileContent(String baseName, List<ParsedLabel> labels) {
    }

    private record ParsedLabel(int classId, float xCenter, float yCenter, float w, float h) {
    }
}
