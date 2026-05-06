package com.trafficsigndetector.datasetservice.repository;

import com.trafficsigndetector.sharedmodel.KhungNhanDang;
import com.trafficsigndetector.sharedmodel.LoaiBien;
import com.trafficsigndetector.sharedmodel.Mau;
import com.trafficsigndetector.sharedmodel.TapDuLieu;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JDBC persistence for dataset catalog (no JPA entities — domain rows map to {@link TapDuLieu} graph).
 */
@Repository
public class DatasetJdbcRepository {

    private final JdbcTemplate jdbc;

    public DatasetJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<TapDuLieu> findFullById(int tapDuLieuId) {
        List<TapDuLieu> roots = jdbc.query(
                """
                        SELECT "Id", "Ten" FROM "TapDuLieu" WHERE "Id" = ?
                        """,
                (rs, rowNum) -> new TapDuLieu(rs.getInt("Id"), rs.getString("Ten"), List.of()),
                tapDuLieuId
        );
        if (roots.isEmpty()) {
            return Optional.empty();
        }
        List<TapDuLieu> hydrated = hydrateDatasets(List.of(roots.get(0)));
        return Optional.of(hydrated.get(0));
    }

    public List<TapDuLieu> findAllFullOrderByIdAsc() {
        List<TapDuLieu> roots = jdbc.query(
                """
                        SELECT "Id", "Ten" FROM "TapDuLieu" ORDER BY "Id" ASC
                        """,
                (rs, rowNum) -> new TapDuLieu(rs.getInt("Id"), rs.getString("Ten"), List.of())
        );
        if (roots.isEmpty()) {
            return List.of();
        }
        return hydrateDatasets(roots);
    }

    public int insertTapDuLieu(String ten) {
        Integer id = jdbc.queryForObject(
                """
                        INSERT INTO "TapDuLieu" ("Ten") VALUES (?) RETURNING "Id"
                        """,
                Integer.class,
                ten
        );
        return Objects.requireNonNull(id);
    }

    public void updateTapDuLieuTen(int tapDuLieuId, String ten) {
        jdbc.update(
                """
                        UPDATE "TapDuLieu" SET "Ten" = ? WHERE "Id" = ?
                        """,
                ten,
                tapDuLieuId
        );
    }

    public void deleteTapDuLieu(int tapDuLieuId) {
        jdbc.update(
                """
                        DELETE FROM "TapDuLieu" WHERE "Id" = ?
                        """,
                tapDuLieuId
        );
    }

    public int insertMau(int tapDuLieuId, String duongDanAnh, String doPhanGiai) {
        Integer id = jdbc.queryForObject(
                """
                        INSERT INTO "Mau" ("DuongDanAnh", "DoPhanGiai", "TapDuLieuId")
                        VALUES (?, ?, ?) RETURNING "Id"
                        """,
                Integer.class,
                duongDanAnh,
                doPhanGiai,
                tapDuLieuId
        );
        return Objects.requireNonNull(id);
    }

    public void deleteMauForDatasetWhereIdIn(int tapDuLieuId, List<Integer> mauIds) {
        if (mauIds == null || mauIds.isEmpty()) {
            return;
        }
        String inClause = mauIds.stream().map(id -> "?").collect(Collectors.joining(","));
        List<Object> args = new ArrayList<>();
        args.addAll(mauIds);
        args.add(tapDuLieuId);
        jdbc.update(
                """
                        DELETE FROM "Mau" WHERE "Id" IN (%s) AND "TapDuLieuId" = ?
                        """.formatted(inClause),
                args.toArray()
        );
    }

    public void insertKhungNhanDang(int mauId, float xCenter, float yCenter, float w, float h, int bienId) {
        jdbc.update(
                """
                        INSERT INTO "KhungNhanDang" ("XCenter", "YCenter", "W", "H", "MauId", "BienId")
                        VALUES (?, ?, ?, ?, ?, ?)
                        """,
                xCenter,
                yCenter,
                w,
                h,
                mauId,
                bienId
        );
    }

    public Map<Integer, LoaiBien> loadLoaiBienById() {
        return jdbc.query(
                """
                        SELECT "Id", "Ten" FROM "LoaiBien"
                        """,
                rs -> {
                    Map<Integer, LoaiBien> map = new HashMap<>();
                    while (rs.next()) {
                        map.put(rs.getInt("Id"), new LoaiBien(rs.getInt("Id"), rs.getString("Ten")));
                    }
                    return map;
                }
        );
    }

    private List<TapDuLieu> hydrateDatasets(List<TapDuLieu> roots) {
        List<Integer> dsIds = roots.stream().map(TapDuLieu::id).filter(Objects::nonNull).toList();
        if (dsIds.isEmpty()) {
            return roots;
        }

        String dsIn = dsIds.stream().map(id -> "?").collect(Collectors.joining(","));
        List<Object> dsArgs = new ArrayList<>(dsIds);

        List<MauRow> mauRows = jdbc.query(
                """
                        SELECT "Id", "DuongDanAnh", "DoPhanGiai", "TapDuLieuId"
                        FROM "Mau"
                        WHERE "TapDuLieuId" IN (%s)
                        ORDER BY "TapDuLieuId" ASC, "Id" ASC
                        """.formatted(dsIn),
                (rs, rowNum) -> new MauRow(
                        rs.getInt("Id"),
                        rs.getString("DuongDanAnh"),
                        rs.getString("DoPhanGiai"),
                        rs.getInt("TapDuLieuId")
                ),
                dsArgs.toArray()
        );

        Map<Integer, List<MauRow>> mauByDataset = new LinkedHashMap<>();
        for (Integer id : dsIds) {
            mauByDataset.put(id, new ArrayList<>());
        }
        for (MauRow row : mauRows) {
            mauByDataset.computeIfAbsent(row.tapDuLieuId(), k -> new ArrayList<>()).add(row);
        }

        List<Integer> allMauIds = mauRows.stream().map(MauRow::id).toList();
        Map<Integer, List<KhungNhanDang>> boxesByMauId = loadBoxesByMauIds(allMauIds);

        List<TapDuLieu> result = new ArrayList<>();
        for (TapDuLieu root : roots) {
            List<Mau> samples = new ArrayList<>();
            List<MauRow> rows = mauByDataset.getOrDefault(root.id(), List.of());
            for (MauRow mr : rows) {
                samples.add(new Mau(mr.id(), mr.duongDanAnh(), mr.doPhanGiai(), boxesByMauId.getOrDefault(mr.id(), List.of())));
            }
            result.add(new TapDuLieu(root.id(), root.ten(), samples));
        }
        return result;
    }

    private Map<Integer, List<KhungNhanDang>> loadBoxesByMauIds(List<Integer> mauIds) {
        Map<Integer, List<KhungNhanDang>> boxesByMauId = new LinkedHashMap<>();
        if (mauIds.isEmpty()) {
            return boxesByMauId;
        }
        String inClause = mauIds.stream().map(id -> "?").collect(Collectors.joining(","));
        List<Object> args = new ArrayList<>(mauIds);

        jdbc.query(
                """
                        SELECT k."Id" AS kid, k."XCenter", k."YCenter", k."W", k."H", k."MauId",
                               l."Id" AS lid, l."Ten" AS lten
                        FROM "KhungNhanDang" k
                        INNER JOIN "LoaiBien" l ON k."BienId" = l."Id"
                        WHERE k."MauId" IN (%s)
                        ORDER BY k."MauId" ASC, k."Id" ASC
                        """.formatted(inClause),
                args.toArray(),
                rs -> {
                    int mauId = rs.getInt("MauId");
                    KhungNhanDang box = new KhungNhanDang(
                            rs.getInt("kid"),
                            rs.getObject("XCenter") != null ? rs.getFloat("XCenter") : null,
                            rs.getObject("YCenter") != null ? rs.getFloat("YCenter") : null,
                            rs.getObject("W") != null ? rs.getFloat("W") : null,
                            rs.getObject("H") != null ? rs.getFloat("H") : null,
                            new LoaiBien(rs.getInt("lid"), rs.getString("lten"))
                    );
                    boxesByMauId.computeIfAbsent(mauId, k -> new ArrayList<>()).add(box);
                }
        );

        return boxesByMauId;
    }

    private record MauRow(int id, String duongDanAnh, String doPhanGiai, int tapDuLieuId) {
    }
}
