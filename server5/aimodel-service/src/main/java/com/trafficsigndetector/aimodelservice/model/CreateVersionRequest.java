package com.trafficsigndetector.aimodelservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateVersionRequest(
        @JsonProperty("Ten") 
        @NotBlank(message = "Tên phiên bản không được để trống") 
        String ten,
        
        @JsonProperty("MoTa") 
        String moTa,
        
        @JsonProperty("DuongDanMH") 
        @NotBlank(message = "Đường dẫn mô hình không được để trống") 
        String duongDanMH
) {
}
