package com.trafficsigndetector.trainingorchestratorservice.persistence.sqlite;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * SQLite read-model cache for training sessions.
 * Synced from PostgreSQL via RabbitMQ events.
 * Used for fast reads during tuần 1-3, then becomes primary during tuần 4.
 */
@Entity
@Table(name = "training_session_cache")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingSessionSqliteEntity {

    @Id
    private Integer id;

    @Column(name = "tracking_id", nullable = false, unique = true, length = 64)
    private String trackingId;

    @Column(name = "trang_thai", nullable = false, length = 32)
    private String trangThai;

    @Column(name = "epochs", nullable = false)
    private Integer epochs;

    @Column(name = "batch_size", nullable = false)
    private Integer batchSize;

    @Column(name = "learning_rate", nullable = false)
    private Double learningRate;

    @Column(name = "kich_thuoc_anh", nullable = false)
    private Integer kichThuocAnh;

    @Column(name = "loai_thiet_bi", nullable = false, length = 32)
    private String loaiThietBi;

    @Column(name = "early_stopping_patience", nullable = false)
    private Integer earlyStoppingPatience;

    @Column(name = "optimizer", nullable = false, length = 64)
    private String optimizer;

    @Column(name = "current_epoch")
    private Integer currentEpoch;

    @Column(name = "do_chinh_xac")
    private Double doChinhXac;

    @Column(name = "do_nhay")
    private Double doNhay;

    @Column(name = "bat_dau_luc")
    private Instant batDauLuc;

    @Column(name = "ket_thuc_luc")
    private Instant ketThucLuc;

    @Column(name = "duong_dan_mo_hinh_ket_qua", length = 500)
    private String duongDanMoHinhKetQua;

    @Column(name = "mo_hinh_hl_json")
    private String moHinhHLJson;

    @Column(name = "phien_ban_hl_json")
    private String phienBanHLJson;

    @Column(name = "ds_mau_hl_json")
    private String dsMauHLJson;

    @Column(name = "synced_at", nullable = false)
    @Builder.Default
    private Instant syncedAt = Instant.now();

    @Column(name = "sync_version", nullable = false)
    @Builder.Default
    private Long syncVersion = 0L;
}
