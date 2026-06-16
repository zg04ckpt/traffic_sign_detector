package com.trafficsigndetector.sharedmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Mau(
        @JsonProperty("Id") Integer id,
        @JsonProperty("DuongDanAnh") String duongDanAnh,
        @JsonProperty("DoPhanGiai") String doPhanGiai,
        @JsonProperty("DsBien") List<KhungNhanDang> dsBien
) {
}
