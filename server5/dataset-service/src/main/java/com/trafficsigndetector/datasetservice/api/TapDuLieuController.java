package com.trafficsigndetector.datasetservice.api;

import com.trafficsigndetector.datasetservice.model.Mau;
import com.trafficsigndetector.datasetservice.model.TapDuLieu;
import com.trafficsigndetector.datasetservice.service.DatasetCatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TapDuLieuController {

    private static final Logger log = LoggerFactory.getLogger(TapDuLieuController.class);

    private final DatasetCatalogService datasetCatalogService;

    public TapDuLieuController(DatasetCatalogService datasetCatalogService) {
        this.datasetCatalogService = datasetCatalogService;
    }

    @GetMapping("/tap-du-lieu")
    public List<TapDuLieu> getTapDuLieu() {
        log.debug("GET /api/tap-du-lieu");
        return datasetCatalogService.getAllDatasets();
    }

    @GetMapping("/tap-du-lieu/{tapDuLieuId}")
    public TapDuLieu getTapDuLieuById(@PathVariable int tapDuLieuId) {
        log.debug("GET /api/tap-du-lieu/{}", tapDuLieuId);
        return datasetCatalogService.getDatasetById(tapDuLieuId);
    }

    @PostMapping(value = "/tap-du-lieu", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public TapDuLieu createDataset(
            @RequestParam("tenDataset") String tenDataset,
            @RequestParam("images") List<MultipartFile> images,
            @RequestParam(value = "labels", required = false) List<MultipartFile> labels
    ) {
        int imageCount = images == null ? 0 : images.size();
        int labelCount = labels == null ? 0 : labels.size();
        log.info("POST /api/tap-du-lieu tenDataset='{}' images={} labels={}", tenDataset, imageCount, labelCount);
        return datasetCatalogService.createDataset(tenDataset, images, labels);
    }

    @PutMapping(value = "/tap-du-lieu/{tapDuLieuId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TapDuLieu updateDataset(
            @PathVariable int tapDuLieuId,
            @RequestParam(value = "tenDataset", required = false) String tenDataset,
            @RequestParam(value = "addImages", required = false) List<MultipartFile> addImages,
            @RequestParam(value = "labels", required = false) List<MultipartFile> labels,
            @RequestParam(value = "removeSampleIds", required = false) List<Integer> removeSampleIds
    ) {
        int addImageCount = addImages == null ? 0 : addImages.size();
        int labelCount = labels == null ? 0 : labels.size();
        int removeCount = removeSampleIds == null ? 0 : removeSampleIds.size();
        log.info(
                "PUT /api/tap-du-lieu/{} tenDataset='{}' addImages={} labels={} removeSampleIds={}",
                tapDuLieuId,
                tenDataset,
                addImageCount,
                labelCount,
                removeCount
        );
        return datasetCatalogService.updateDataset(tapDuLieuId, tenDataset, addImages, labels, removeSampleIds);
    }

    @DeleteMapping("/tap-du-lieu/{tapDuLieuId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDataset(@PathVariable int tapDuLieuId) {
        log.info("DELETE /api/tap-du-lieu/{}", tapDuLieuId);
        datasetCatalogService.deleteDataset(tapDuLieuId);
    }

    @PostMapping(value = "/tap-du-lieu/{tapDuLieuId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
        public Mau uploadSample(
            @PathVariable int tapDuLieuId,
            @RequestParam("file") MultipartFile file
    ) {
        String fileName = file == null ? "null" : file.getOriginalFilename();
        log.info("POST /api/tap-du-lieu/{}/upload file='{}'", tapDuLieuId, fileName);
        return datasetCatalogService.saveSample(tapDuLieuId, file);
    }
}
