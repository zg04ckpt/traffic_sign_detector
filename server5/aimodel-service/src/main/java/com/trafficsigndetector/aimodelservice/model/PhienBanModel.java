package com.trafficsigndetector.aimodelservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PhienBan(
        @JsonProperty("Id") Integer id,
        @JsonProperty("Ten") String ten,
        @JsonProperty("MoTa") String moTa,
        @JsonProperty("DuongDanMH") String duongDanMH
) {
}

