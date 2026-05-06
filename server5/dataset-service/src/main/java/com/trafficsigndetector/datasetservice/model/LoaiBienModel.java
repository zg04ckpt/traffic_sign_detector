package com.trafficsigndetector.datasetservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LoaiBien(
        @JsonProperty("Id") Integer id,
        @JsonProperty("Ten") String ten
) {
}

