package com.trafficsigndetector.datasetservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TapDuLieu(
        @JsonProperty("Id") Integer id,
        @JsonProperty("Ten") String ten,
        @JsonProperty("DsMau") List<Mau> dsMau
) {
}

