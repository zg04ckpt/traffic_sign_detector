package com.trafficsigndetector.trainingorchestratorservice.persistence.jdbc;

import com.trafficsigndetector.trainingorchestratorservice.persistence.sqlite.TrainingSessionSqliteEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainingSessionSqliteJdbcRepository {

    private final JdbcTemplate jdbc;

    public TrainingSessionSqliteJdbcRepository(@Qualifier("sqliteJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<TrainingSessionSqliteEntity> findById(Integer id) {
        List<TrainingSessionSqliteEntity> rows = jdbc.query(
                """
                        SELECT id, tracking_id, trang_thai, epochs, batch_size, learning_rate, kich_thuoc_anh,
                               loai_thiet_bi, early_stopping_patience, optimizer, current_epoch, do_chinh_xac, do_nhay,
                               bat_dau_luc, ket_thuc_luc, duong_dan_mo_hinh_ket_qua, mo_hinh_hl_json, phien_ban_hl_json,
                               ds_mau_hl_json, synced_at, sync_version
                        FROM training_session_cache WHERE id = ?
                        """,
                (rs, rn) -> mapRow(rs),
                id
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public TrainingSessionSqliteEntity save(TrainingSessionSqliteEntity e) {
        jdbc.update(
                """
                        INSERT INTO training_session_cache (
                            id, tracking_id, trang_thai, epochs, batch_size, learning_rate, kich_thuoc_anh,
                            loai_thiet_bi, early_stopping_patience, optimizer, current_epoch, do_chinh_xac, do_nhay,
                            bat_dau_luc, ket_thuc_luc, duong_dan_mo_hinh_ket_qua, mo_hinh_hl_json, phien_ban_hl_json,
                            ds_mau_hl_json, synced_at, sync_version
                        ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                        ON CONFLICT(id) DO UPDATE SET
                            tracking_id = excluded.tracking_id,
                            trang_thai = excluded.trang_thai,
                            epochs = excluded.epochs,
                            batch_size = excluded.batch_size,
                            learning_rate = excluded.learning_rate,
                            kich_thuoc_anh = excluded.kich_thuoc_anh,
                            loai_thiet_bi = excluded.loai_thiet_bi,
                            early_stopping_patience = excluded.early_stopping_patience,
                            optimizer = excluded.optimizer,
                            current_epoch = excluded.current_epoch,
                            do_chinh_xac = excluded.do_chinh_xac,
                            do_nhay = excluded.do_nhay,
                            bat_dau_luc = excluded.bat_dau_luc,
                            ket_thuc_luc = excluded.ket_thuc_luc,
                            duong_dan_mo_hinh_ket_qua = excluded.duong_dan_mo_hinh_ket_qua,
                            mo_hinh_hl_json = excluded.mo_hinh_hl_json,
                            phien_ban_hl_json = excluded.phien_ban_hl_json,
                            ds_mau_hl_json = excluded.ds_mau_hl_json,
                            synced_at = excluded.synced_at,
                            sync_version = excluded.sync_version
                        """,
                e.getId(),
                e.getTrackingId(),
                e.getTrangThai(),
                e.getEpochs(),
                e.getBatchSize(),
                e.getLearningRate(),
                e.getKichThuocAnh(),
                e.getLoaiThietBi(),
                e.getEarlyStoppingPatience(),
                e.getOptimizer(),
                e.getCurrentEpoch(),
                e.getDoChinhXac(),
                e.getDoNhay(),
                e.getBatDauLuc() != null ? e.getBatDauLuc().toString() : null,
                e.getKetThucLuc() != null ? e.getKetThucLuc().toString() : null,
                e.getDuongDanMoHinhKetQua(),
                e.getMoHinhHLJson(),
                e.getPhienBanHLJson(),
                e.getDsMauHLJson(),
                e.getSyncedAt() != null ? e.getSyncedAt().toString() : Instant.now().toString(),
                e.getSyncVersion() != null ? e.getSyncVersion() : 0L
        );
        return e;
    }

    private static TrainingSessionSqliteEntity mapRow(ResultSet rs) throws SQLException {
        return TrainingSessionSqliteEntity.builder()
                .id(rs.getInt("id"))
                .trackingId(rs.getString("tracking_id"))
                .trangThai(rs.getString("trang_thai"))
                .epochs(rs.getInt("epochs"))
                .batchSize(rs.getInt("batch_size"))
                .learningRate(rs.getDouble("learning_rate"))
                .kichThuocAnh(rs.getInt("kich_thuoc_anh"))
                .loaiThietBi(rs.getString("loai_thiet_bi"))
                .earlyStoppingPatience(rs.getInt("early_stopping_patience"))
                .optimizer(rs.getString("optimizer"))
                .currentEpoch((Integer) rs.getObject("current_epoch"))
                .doChinhXac((Double) rs.getObject("do_chinh_xac"))
                .doNhay((Double) rs.getObject("do_nhay"))
                .batDauLuc(parseInstant(rs.getString("bat_dau_luc")))
                .ketThucLuc(parseInstant(rs.getString("ket_thuc_luc")))
                .duongDanMoHinhKetQua(rs.getString("duong_dan_mo_hinh_ket_qua"))
                .moHinhHLJson(rs.getString("mo_hinh_hl_json"))
                .phienBanHLJson(rs.getString("phien_ban_hl_json"))
                .dsMauHLJson(rs.getString("ds_mau_hl_json"))
                .syncedAt(parseInstant(rs.getString("synced_at")))
                .syncVersion(rs.getLong("sync_version"))
                .build();
    }

    private static Instant parseInstant(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(raw);
        } catch (Exception ex) {
            return null;
        }
    }
}
