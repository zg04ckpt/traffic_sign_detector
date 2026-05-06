package com.trafficsigndetector.datasetservice.service;

import com.trafficsigndetector.datasetservice.entity.KhungNhanDangEntity;
import com.trafficsigndetector.datasetservice.entity.LoaiBienEntity;
import com.trafficsigndetector.datasetservice.entity.MauEntity;
import com.trafficsigndetector.datasetservice.entity.TapDuLieuEntity;
import com.trafficsigndetector.datasetservice.model.KhungNhanDang;
import com.trafficsigndetector.datasetservice.model.LoaiBien;
import com.trafficsigndetector.datasetservice.model.Mau;
import com.trafficsigndetector.datasetservice.model.TapDuLieu;
import com.trafficsigndetector.datasetservice.repository.LoaiBienRepository;
import com.trafficsigndetector.datasetservice.repository.TapDuLieuRepository;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class DatasetCatalogService {

    private static final Logger log = LoggerFactory.getLogger(DatasetCatalogService.class);

    private final TapDuLieuRepository tapDuLieuRepository;
    private final LoaiBienRepository loaiBienRepository;
    private final Path uploadRoot;

    public DatasetCatalogService(
            TapDuLieuRepository tapDuLieuRepository,
            LoaiBienRepository loaiBienRepository,
            @Value("${app.storage.upload-dir:dataset-service/uploads}") String uploadDir
    ) {
        this.tapDuLieuRepository = tapDuLieuRepository;
        this.loaiBienRepository = loaiBienRepository;
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

        TapDuLieuEntity dataset = new TapDuLieuEntity();
        dataset.setTen(datasetName);
        dataset = tapDuLieuRepository.save(dataset);

        try {
            addUploadedSamples(dataset, images, normalizeFiles(labelFiles));
            TapDuLieuEntity saved = tapDuLieuRepository.save(dataset);
            log.info("Created dataset id={} name='{}' samples={}", saved.getId(), saved.getTen(), saved.getDsMau().size());
            return toDatasetResponse(saved);
        } catch (RuntimeException ex) {
            log.error("Create dataset failed for id={} name='{}'", dataset.getId(), datasetName, ex);
            cleanupDatasetFolder(dataset.getId());
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
        TapDuLieuEntity dataset = requireDataset(datasetId);

        if (tenDataset != null && !tenDataset.isBlank()) {
            dataset.setTen(normalizeDatasetName(tenDataset));
        }

        removeSamples(dataset, removeSampleIds);
        addUploadedSamples(dataset, normalizeFiles(addImages), normalizeFiles(labelFiles));

        TapDuLieuEntity saved = tapDuLieuRepository.save(dataset);
        return toDatasetResponse(saved);
    }

    @Transactional
    public void deleteDataset(int datasetId) {
        TapDuLieuEntity dataset = requireDataset(datasetId);
        cleanupDatasetFolder(dataset.getId());
        tapDuLieuRepository.delete(dataset);
    }

    @Transactional(readOnly = true)
    public List<TapDuLieu> getAllDatasets() {
        List<TapDuLieuEntity> datasets = tapDuLieuRepository.findAllByOrderByIdAsc();
        List<TapDuLieu> list = new ArrayList<>();
        for (TapDuLieuEntity dataset : datasets) {
            list.add(toDatasetResponse(dataset));
        }
        return list;
    }

    @Transactional(readOnly = true)
    public TapDuLieu getDatasetById(int datasetId) {
        return toDatasetResponse(requireDataset(datasetId));
    }

    @Transactional
    public Mau saveSample(int datasetId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File upload is empty");
        }

        TapDuLieuEntity dataset = requireDataset(datasetId);

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

        MauEntity sample = new MauEntity();
        sample.setDuongDanAnh("/uploads/dataset-" + datasetId + "/" + generated);
        sample.setDoPhanGiai("unknown");
        dataset.addMau(sample);

        TapDuLieuEntity saved = tapDuLieuRepository.save(dataset);

        MauEntity created = null;
        for (MauEntity item : saved.getDsMau()) {
            if (sample.getDuongDanAnh().equals(item.getDuongDanAnh())) {
                created = item;
            }
        }

        if (created == null) {
            throw new IllegalStateException("Could not persist uploaded sample");
        }

        return toSampleResponse(created);
    }

    public String getUploadRoot() {
        return uploadRoot.toString();
    }

    private TapDuLieu toDatasetResponse(TapDuLieuEntity dataset) {
        List<Mau> samples = new ArrayList<>();
        for (MauEntity sample : dataset.getDsMau()) {
            samples.add(toSampleResponse(sample));
        }
        return new TapDuLieu(dataset.getId(), dataset.getTen(), samples);
    }

    private Mau toSampleResponse(MauEntity sample) {
        List<KhungNhanDang> boxes = new ArrayList<>();
        for (KhungNhanDangEntity box : sample.getDsBien()) {
            boxes.add(toBoxResponse(box));
        }
        return new Mau(sample.getId(), sample.getDuongDanAnh(), sample.getDoPhanGiai(), boxes);
    }

    private KhungNhanDang toBoxResponse(KhungNhanDangEntity box) {
        LoaiBien bien = null;
        if (box.getBien() != null) {
            bien = new LoaiBien(box.getBien().getId(), box.getBien().getTen());
        }
        return new KhungNhanDang(
                box.getId(),
                box.getXCenter(),
                box.getYCenter(),
                box.getW(),
                box.getH(),
                bien
        );
    }

    private void initStorage() {
        try {
            Files.createDirectories(uploadRoot);
            log.info("Dataset upload root: {}", uploadRoot);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot initialize upload directory: " + uploadRoot, ex);
        }
    }

    private TapDuLieuEntity requireDataset(int datasetId) {
        return tapDuLieuRepository.findById(datasetId)
                .orElseThrow(() -> new IllegalStateException("Dataset not found: " + datasetId));
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

    private void removeSamples(TapDuLieuEntity dataset, List<Integer> removeSampleIds) {
        if (removeSampleIds == null || removeSampleIds.isEmpty()) {
            return;
        }
        Set<Integer> removeSet = new HashSet<>(removeSampleIds);
        Iterator<MauEntity> iterator = dataset.getDsMau().iterator();
        while (iterator.hasNext()) {
            MauEntity sample = iterator.next();
            if (sample.getId() != null && removeSet.contains(sample.getId())) {
                deleteSampleFileIfExists(dataset.getId(), sample.getDuongDanAnh());
                iterator.remove();
                sample.setTapDuLieu(null);
            }
        }
    }

    private void addUploadedSamples(TapDuLieuEntity dataset, List<MultipartFile> imageFiles, List<MultipartFile> labelFiles) {
        if (imageFiles.isEmpty()) {
            return;
        }

        List<LabelFileContent> parsedLabels = parseLabelFiles(labelFiles);
        Map<String, List<ParsedLabel>> labelsByImageName = new HashMap<>();
        for (LabelFileContent item : parsedLabels) {
            labelsByImageName.putIfAbsent(item.baseName(), item.labels());
        }
        boolean useIndexFallback = !parsedLabels.isEmpty() && parsedLabels.size() == imageFiles.size();

        Map<Integer, LoaiBienEntity> loaiBienMap = new HashMap<>();
        for (LoaiBienEntity loaiBien : loaiBienRepository.findAll()) {
            loaiBienMap.put(loaiBien.getId(), loaiBien);
        }

        Path datasetFolder = uploadRoot.resolve("dataset-" + dataset.getId());
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

            MauEntity sample = new MauEntity();
            sample.setDuongDanAnh("/uploads/dataset-" + dataset.getId() + "/" + generated);
            sample.setDoPhanGiai("unknown");

            String imageBaseName = extractBaseName(image.getOriginalFilename());
            List<ParsedLabel> labels = labelsByImageName.get(imageBaseName);
            if (labels == null && useIndexFallback) {
                labels = parsedLabels.get(i).labels();
            }
            if (labels != null && !labels.isEmpty()) {
                attachLabels(sample, labels, loaiBienMap, image.getOriginalFilename());
            }

            dataset.addMau(sample);
        }
    }

    private void attachLabels(
            MauEntity sample,
            List<ParsedLabel> labels,
            Map<Integer, LoaiBienEntity> loaiBienMap,
            String sourceName
    ) {
        for (ParsedLabel label : labels) {
            LoaiBienEntity loaiBien = loaiBienMap.get(label.classId());
            if (loaiBien == null) {
                throw new IllegalArgumentException("Label file " + sourceName + " chứa classId không hợp lệ: " + label.classId());
            }

            KhungNhanDangEntity box = new KhungNhanDangEntity();
            box.setXCenter(label.xCenter());
            box.setYCenter(label.yCenter());
            box.setW(label.w());
            box.setH(label.h());
            box.setBien(loaiBien);
            box.setMau(sample);
            sample.getDsBien().add(box);
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

