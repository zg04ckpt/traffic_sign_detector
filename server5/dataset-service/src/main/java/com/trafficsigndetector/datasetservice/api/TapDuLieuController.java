package com.trafficsigndetector.datasetservice.api;

import com.trafficsigndetector.sharedmodel.Mau;
import com.trafficsigndetector.sharedmodel.TapDuLieu;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.trafficsigndetector.datasetservice.dto.DatasetCreateRequestDTO;
import com.trafficsigndetector.datasetservice.dto.DatasetUpdateRequestDTO;

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
    public TapDuLieu createDataset(@ModelAttribute DatasetCreateRequestDTO request) {
        int imageCount = request.getImages() == null ? 0 : request.getImages().size();
        int labelCount = request.getLabels() == null ? 0 : request.getLabels().size();
        log.info("POST /api/tap-du-lieu tenDataset='{}' images={} labels={}", request.getTenDataset(), imageCount, labelCount);
        return datasetCatalogService.createDataset(request.getTenDataset(), request.getImages(), request.getLabels());
    }

    @PutMapping(value = "/tap-du-lieu/{tapDuLieuId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TapDuLieu updateDataset(
            @PathVariable int tapDuLieuId,
            @ModelAttribute DatasetUpdateRequestDTO request
    ) {
        int addImageCount = request.getAddImages() == null ? 0 : request.getAddImages().size();
        int labelCount = request.getLabels() == null ? 0 : request.getLabels().size();
        int removeCount = request.getRemoveSampleIds() == null ? 0 : request.getRemoveSampleIds().size();
        log.info(
                "PUT /api/tap-du-lieu/{} tenDataset='{}' addImages={} labels={} removeSampleIds={}",
                tapDuLieuId,
                request.getTenDataset(),
                addImageCount,
                labelCount,
                removeCount
        );
        return datasetCatalogService.updateDataset(tapDuLieuId, request.getTenDataset(), request.getAddImages(), request.getLabels(), request.getRemoveSampleIds());
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
