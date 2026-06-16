package com.trafficsigndetector.aimodelservice.repository;

import com.trafficsigndetector.sharedmodel.MoHinh;
import com.trafficsigndetector.sharedmodel.PhienBan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * JDBC persistence mapping rows to {@link MoHinh} / {@link PhienBan} shared model records.
 */
@Repository
public class ModelJdbcRepository {

    private final JdbcTemplate jdbc;

    public ModelJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean moHinhExists(int moHinhId) {
        Integer n = jdbc.queryForObject(
                """
                        SELECT COUNT(*) FROM "MoHinh" WHERE "Id" = ?
                        """,
                Integer.class,
                moHinhId
        );
        return n != null && n > 0;
    }

    public int insertPhienBan(int moHinhId, String ten, String moTa, String duongDanMH) {
        Integer id = jdbc.queryForObject(
                """
                        INSERT INTO "PhienBan" ("Ten", "MoTa", "DuongDanMH", "MoHinhId")
                        VALUES (?, ?, ?, ?) RETURNING "Id"
                        """,
                Integer.class,
                ten,
                moTa,
                duongDanMH,
                moHinhId
        );
        return Objects.requireNonNull(id);
    }

    public List<MoHinh> findAllFullOrderByIdAsc() {
        List<MoHinh> roots = jdbc.query(
                """
                        SELECT "Id", "Ten", "MoHinhGoc" FROM "MoHinh" ORDER BY "Id" ASC
                        """,
                (rs, rowNum) -> new MoHinh(
                        rs.getInt("Id"),
                        rs.getString("Ten"),
                        rs.getString("MoHinhGoc"),
                        List.of()
                )
        );
        if (roots.isEmpty()) {
            return List.of();
        }

        List<Integer> modelIds = roots.stream().map(MoHinh::id).toList();
        String inClause = modelIds.stream().map(id -> "?").collect(Collectors.joining(","));

        List<PhienBanRow> versionRows = jdbc.query(
                """
                        SELECT "Id", "Ten", "MoTa", "DuongDanMH", "MoHinhId"
                        FROM "PhienBan"
                        WHERE "MoHinhId" IN (%s)
                        ORDER BY "MoHinhId" ASC, "Id" ASC
                        """.formatted(inClause),
                (rs, rowNum) -> new PhienBanRow(
                        rs.getInt("Id"),
                        rs.getString("Ten"),
                        rs.getString("MoTa"),
                        rs.getString("DuongDanMH"),
                        rs.getInt("MoHinhId")
                ),
                modelIds.toArray()
        );

        Map<Integer, List<PhienBanRow>> byModel = new LinkedHashMap<>();
        for (Integer mid : modelIds) {
            byModel.put(mid, new ArrayList<>());
        }
        for (PhienBanRow row : versionRows) {
            byModel.computeIfAbsent(row.moHinhId(), k -> new ArrayList<>()).add(row);
        }

        List<MoHinh> result = new ArrayList<>();
        for (MoHinh root : roots) {
            List<PhienBanRow> rows = byModel.getOrDefault(root.id(), List.of());
            List<PhienBanRow> sorted = new ArrayList<>(rows);
            sorted.sort(Comparator.comparing(PhienBanRow::id, Comparator.nullsLast(Integer::compareTo)).reversed());
            List<PhienBan> versions = new ArrayList<>();
            for (PhienBanRow r : sorted) {
                versions.add(new PhienBan(r.id(), r.ten(), r.moTa(), r.duongDanMH()));
            }
            result.add(new MoHinh(root.id(), root.ten(), root.moHinhGoc(), versions));
        }
        return result;
    }

    private record PhienBanRow(int id, String ten, String moTa, String duongDanMH, int moHinhId) {
    }
}
