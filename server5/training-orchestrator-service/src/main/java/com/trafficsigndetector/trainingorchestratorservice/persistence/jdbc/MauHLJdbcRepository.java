package com.trafficsigndetector.trainingorchestratorservice.persistence.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MauHLJdbcRepository {

    private final JdbcTemplate jdbc;

    public MauHLJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void deleteByThongTinHLId(int thongTinHLId) {
        jdbc.update("DELETE FROM mau_hl WHERE thong_tin_hl_id = ?", thongTinHLId);
    }

    public void insertRows(int thongTinHLId, List<String> thongTinMauJsonLines) {
        if (thongTinMauJsonLines == null || thongTinMauJsonLines.isEmpty()) {
            return;
        }
        for (String json : thongTinMauJsonLines) {
            jdbc.update(
                    """
                            INSERT INTO mau_hl (thong_tin_hl_id, thong_tin_mau_json) VALUES (?, ?)
                            """,
                    thongTinHLId,
                    json
            );
        }
    }

    public List<String> findThongTinMauJsonOrdered(int thongTinHLId) {
        return jdbc.query(
                """
                        SELECT thong_tin_mau_json FROM mau_hl WHERE thong_tin_hl_id = ? ORDER BY id ASC
                        """,
                (rs, rowNum) -> rs.getString("thong_tin_mau_json"),
                thongTinHLId
        );
    }
}
