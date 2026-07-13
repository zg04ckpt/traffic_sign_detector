package com.trafficsigndetector.datasetservice.dto;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public class DatasetUpdateRequestDTO {
    private String tenDataset;
    private List<MultipartFile> addImages;
    private List<MultipartFile> labels;
    private List<Integer> removeSampleIds;

    public String getTenDataset() {
        return tenDataset;
    }

    public void setTenDataset(String tenDataset) {
        this.tenDataset = tenDataset;
    }

    public List<MultipartFile> getAddImages() {
        return addImages;
    }

    public void setAddImages(List<MultipartFile> addImages) {
        this.addImages = addImages;
    }

    public List<MultipartFile> getLabels() {
        return labels;
    }

    public void setLabels(List<MultipartFile> labels) {
        this.labels = labels;
    }

    public List<Integer> getRemoveSampleIds() {
        return removeSampleIds;
    }

    public void setRemoveSampleIds(List<Integer> removeSampleIds) {
        this.removeSampleIds = removeSampleIds;
    }
}
