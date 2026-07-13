package com.trafficsigndetector.datasetservice.dto;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public class DatasetCreateRequestDTO {
    private String tenDataset;
    private List<MultipartFile> images;
    private List<MultipartFile> labels;

    public String getTenDataset() {
        return tenDataset;
    }

    public void setTenDataset(String tenDataset) {
        this.tenDataset = tenDataset;
    }

    public List<MultipartFile> getImages() {
        return images;
    }

    public void setImages(List<MultipartFile> images) {
        this.images = images;
    }

    public List<MultipartFile> getLabels() {
        return labels;
    }

    public void setLabels(List<MultipartFile> labels) {
        this.labels = labels;
    }
}
