package com.trafficsigndetector.trainingorchestratorservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MauHL(
        @JsonProperty("Id") Integer id,
        @JsonProperty("ThongTin") Mau thongTin,
        @JsonProperty("DuongDanAnh") String duongDanAnh,
        @JsonProperty("DoPhanGiai") String doPhanGiai,
        @JsonProperty("DsBien") List<KhungNhanDang> dsBien
) {
    @JsonIgnore
    public Mau resolvedThongTin() {
        if (thongTin != null) {
            return thongTin;
        }
        return new Mau(id, duongDanAnh, doPhanGiai, dsBien);
    }
}

