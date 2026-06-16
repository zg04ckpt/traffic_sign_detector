package com.trafficsigndetector.trainingorchestratorservice.persistence.sqlite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * SQLite cache row ({@code training_session_cache}). Plain DTO — no JPA.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingSessionSqliteEntity {

    private Integer id;

    private String trackingId;

    private String trangThai;

    private Integer epochs;

    private Integer batchSize;

    private Double learningRate;

    private Integer kichThuocAnh;

    private String loaiThietBi;

    private Integer earlyStoppingPatience;

    private String optimizer;

    private Integer currentEpoch;

    private Double doChinhXac;

    private Double doNhay;

    private Instant batDauLuc;

    private Instant ketThucLuc;

    private String duongDanMoHinhKetQua;

    private String moHinhHLJson;

    private String phienBanHLJson;

    private String dsMauHLJson;

    @Builder.Default
    private Instant syncedAt = Instant.now();

    @Builder.Default
    private Long syncVersion = 0L;
}
