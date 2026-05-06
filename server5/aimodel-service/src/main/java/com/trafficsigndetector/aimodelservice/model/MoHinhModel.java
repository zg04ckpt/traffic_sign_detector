package com.trafficsigndetector.aimodelservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MoHinh(
        @JsonProperty("Id") Integer id,
        @JsonProperty("Ten") String ten,
        @JsonProperty("MoHinhGoc") String moHinhGoc,
        @JsonProperty("DsPhienBan") List<PhienBan> dsPhienBan
) {
}

