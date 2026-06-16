package com.trafficsigndetector.trainingorchestratorservice.persistence.jdbc;

import com.trafficsigndetector.trainingorchestratorservice.persistence.entity.ThongTinHLEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainingSessionJdbcRepository {

    private final JdbcTemplate jdbc;

    public TrainingSessionJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<ThongTinHLEntity> findById(int id) {
        List<ThongTinHLEntity> rows = jdbc.query(
                """
                        SELECT id, tracking_id, epochs, batch_size, trang_thai, bat_dau_luc, ket_thuc_luc,
                               do_chinh_xac, do_nhay, current_epoch, learning_rate, kich_thuoc_anh, loai_thiet_bi,
                               early_stopping_patience, optimizer, phien_ban_hl_json, mo_hinh_hl_json, ds_mau_hl_json,
                               duong_dan_mo_hinh_ket_qua
                        FROM training_session WHERE id = ?
                        """,
                (rs, rowNum) -> mapRow(rs),
                id
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public Optional<ThongTinHLEntity> findByTrackingId(String trackingId) {
        List<ThongTinHLEntity> rows = jdbc.query(
                """
                        SELECT id, tracking_id, epochs, batch_size, trang_thai, bat_dau_luc, ket_thuc_luc,
                               do_chinh_xac, do_nhay, current_epoch, learning_rate, kich_thuoc_anh, loai_thiet_bi,
                               early_stopping_patience, optimizer, phien_ban_hl_json, mo_hinh_hl_json, ds_mau_hl_json,
                               duong_dan_mo_hinh_ket_qua
                        FROM training_session WHERE tracking_id = ?
                        """,
                (rs, rowNum) -> mapRow(rs),
                trackingId
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public long countByTrangThai(String trangThai) {
        Long n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM training_session WHERE trang_thai = ?",
                Long.class,
                trangThai
        );
        return n == null ? 0L : n;
    }

    public long countByTrangThaiIn(List<String> states) {
        if (states == null || states.isEmpty()) {
            return 0L;
        }
        String placeholders = String.join(",", states.stream().map(s -> "?").toList());
        List<Object> args = new ArrayList<>(states);
        Long n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM training_session WHERE trang_thai IN (" + placeholders + ")",
                Long.class,
                args.toArray()
        );
        return n == null ? 0L : n;
    }

    public ThongTinHLEntity save(ThongTinHLEntity session) {
        if (session.getId() == null) {
            return insert(session);
        }
        update(session);
        return session;
    }

    private ThongTinHLEntity insert(ThongTinHLEntity session) {
        Integer id = jdbc.queryForObject(
                """
                        INSERT INTO training_session (
                            tracking_id, epochs, batch_size, trang_thai, bat_dau_luc, ket_thuc_luc,
                            do_chinh_xac, do_nhay, current_epoch, learning_rate, kich_thuoc_anh, loai_thiet_bi,
                            early_stopping_patience, optimizer, phien_ban_hl_json, mo_hinh_hl_json, ds_mau_hl_json,
                            duong_dan_mo_hinh_ket_qua
                        ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                        RETURNING id
                        """,
                Integer.class,
                session.getTrackingId(),
                session.getEpochs(),
                session.getBatchSize(),
                session.getTrangThai(),
                toTs(session.getBatDauLuc()),
                toTs(session.getKetThucLuc()),
                session.getDoChinhXac(),
                session.getDoNhay(),
                session.getCurrentEpoch(),
                session.getLearningRate(),
                session.getKichThuocAnh(),
                session.getLoaiThietBi(),
                session.getEarlyStoppingPatience(),
                session.getOptimizer(),
                session.getPhienBanHLJson(),
                session.getMoHinhHLJson(),
                session.getDsMauHLJson(),
                session.getDuongDanMoHinhKetQua()
        );
        session.setId(id);
        return session;
    }

    private void update(ThongTinHLEntity session) {
        jdbc.update(
                """
                        UPDATE training_session SET
                            tracking_id = ?, epochs = ?, batch_size = ?, trang_thai = ?, bat_dau_luc = ?, ket_thuc_luc = ?,
                            do_chinh_xac = ?, do_nhay = ?, current_epoch = ?, learning_rate = ?, kich_thuoc_anh = ?,
                            loai_thiet_bi = ?, early_stopping_patience = ?, optimizer = ?, phien_ban_hl_json = ?,
                            mo_hinh_hl_json = ?, ds_mau_hl_json = ?, duong_dan_mo_hinh_ket_qua = ?
                        WHERE id = ?
                        """,
                session.getTrackingId(),
                session.getEpochs(),
                session.getBatchSize(),
                session.getTrangThai(),
                toTs(session.getBatDauLuc()),
                toTs(session.getKetThucLuc()),
                session.getDoChinhXac(),
                session.getDoNhay(),
                session.getCurrentEpoch(),
                session.getLearningRate(),
                session.getKichThuocAnh(),
                session.getLoaiThietBi(),
                session.getEarlyStoppingPatience(),
                session.getOptimizer(),
                session.getPhienBanHLJson(),
                session.getMoHinhHLJson(),
                session.getDsMauHLJson(),
                session.getDuongDanMoHinhKetQua(),
                session.getId()
        );
    }

    private static Timestamp toTs(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private static Instant toInstant(Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }

    private static ThongTinHLEntity mapRow(ResultSet rs) throws SQLException {
        ThongTinHLEntity e = new ThongTinHLEntity();
        e.setId(rs.getInt("id"));
        e.setTrackingId(rs.getString("tracking_id"));
        e.setEpochs(rs.getInt("epochs"));
        e.setBatchSize(rs.getInt("batch_size"));
        e.setTrangThai(rs.getString("trang_thai"));
        e.setBatDauLuc(toInstant(rs.getTimestamp("bat_dau_luc")));
        e.setKetThucLuc(toInstant(rs.getTimestamp("ket_thuc_luc")));
        double docx = rs.getDouble("do_chinh_xac");
        e.setDoChinhXac(rs.wasNull() ? null : docx);
        double don = rs.getDouble("do_nhay");
        e.setDoNhay(rs.wasNull() ? null : don);
        int ce = rs.getInt("current_epoch");
        e.setCurrentEpoch(rs.wasNull() ? null : ce);
        e.setLearningRate(rs.getDouble("learning_rate"));
        e.setKichThuocAnh(rs.getInt("kich_thuoc_anh"));
        e.setLoaiThietBi(rs.getString("loai_thiet_bi"));
        e.setEarlyStoppingPatience(rs.getInt("early_stopping_patience"));
        e.setOptimizer(rs.getString("optimizer"));
        e.setPhienBanHLJson(rs.getString("phien_ban_hl_json"));
        e.setMoHinhHLJson(rs.getString("mo_hinh_hl_json"));
        e.setDsMauHLJson(rs.getString("ds_mau_hl_json"));
        e.setDuongDanMoHinhKetQua(rs.getString("duong_dan_mo_hinh_ket_qua"));
        return e;
    }
}
