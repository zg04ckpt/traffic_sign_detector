package com.trafficsigndetector.datasetservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KhungNhanDang(
        @JsonProperty("Id") Integer id,
        @JsonProperty("XCenter") Float xCenter,
        @JsonProperty("YCenter") Float yCenter,
        @JsonProperty("W") Float w,
        @JsonProperty("H") Float h,
        @JsonProperty("Bien") LoaiBien bien
) {
}

