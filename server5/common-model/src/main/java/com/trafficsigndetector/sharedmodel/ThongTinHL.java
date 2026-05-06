package com.trafficsigndetector.sharedmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ThongTinHL(
        @JsonProperty("Id") Integer id,
        @JsonProperty("TrackingId") String trackingId,
        @JsonProperty("Epochs") Integer epochs,
        @JsonProperty("BatchSize") Integer batchSize,
        @JsonProperty("TrangThai") String trangThai,
        @JsonProperty("BatDauLuc") Instant batDauLuc,
        @JsonProperty("KetThucLuc") Instant ketThucLuc,
        @JsonProperty("DoChinhXac") Double doChinhXac,
        @JsonProperty("DoNhay") Double doNhay,
        @JsonProperty("CurrentEpoch") Integer currentEpoch,
        @JsonProperty("LearningRate") Double learningRate,
        @JsonProperty("KichThuocAnh") Integer kichThuocAnh,
        @JsonProperty("LoaiThietBi") String loaiThietBi,
        @JsonProperty("EarlyStoppingPatience") Integer earlyStoppingPatience,
        @JsonProperty("Optimizer") String optimizer,
        @JsonProperty("MoHinhHL") MoHinh moHinhHL,
        @JsonProperty("PhienBanHL") PhienBan phienBanHL,
        @JsonProperty("DsMauHL") List<MauHL> dsMauHL,
        @JsonProperty("DuongDanMoHinhKetQua") String duongDanMoHinhKetQua
) {
}
