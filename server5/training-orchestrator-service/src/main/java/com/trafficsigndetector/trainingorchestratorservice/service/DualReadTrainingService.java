package com.trafficsigndetector.trainingorchestratorservice.service;

import com.trafficsigndetector.trainingorchestratorservice.persistence.entity.ThongTinHLEntity;
import com.trafficsigndetector.trainingorchestratorservice.persistence.jdbc.TrainingSessionJdbcRepository;
import com.trafficsigndetector.trainingorchestratorservice.persistence.jdbc.TrainingSessionSqliteJdbcRepository;
import com.trafficsigndetector.trainingorchestratorservice.persistence.sqlite.TrainingSessionSqliteEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service để read từ cả 2 sources (SQLite + PostgreSQL).
 * 
 * Tuần 1: Read PostgreSQL chính, SQLite là shadow
 * Tuần 2-3: Dual-write enable, read sẽ test-read SQLite với percentage
 * Tuần 4: Flip - read SQLite chính, PostgreSQL là fallback
 */
@Service
@Slf4j
public class DualReadTrainingService {

    private final TrainingSessionJdbcRepository trainingSessionJdbcRepository;
    private final TrainingSessionSqliteJdbcRepository trainingSessionSqliteJdbcRepository;

    @Value("${app.migration.sqlite-enabled:false}")
    private boolean sqliteEnabled;

    @Value("${app.migration.fallback-to-postgres:true}")
    private boolean fallbackToPostgres;

    @Value("${app.migration.dual-write-enabled:false}")
    private boolean dualWriteEnabled;

    public DualReadTrainingService(
            TrainingSessionJdbcRepository trainingSessionJdbcRepository,
            TrainingSessionSqliteJdbcRepository trainingSessionSqliteJdbcRepository) {
        this.trainingSessionJdbcRepository = trainingSessionJdbcRepository;
        this.trainingSessionSqliteJdbcRepository = trainingSessionSqliteJdbcRepository;
    }

    /**
     * Phase 1 (Tuần 1): Primary = PostgreSQL, Shadow = SQLite
     * Phase 2-3 (Tuần 2-3): Dual-write = true, test-read SQLite (logging mismatch)
     * Phase 4 (Tuần 4): Primary = SQLite, Fallback = PostgreSQL
     */
    public Optional<ThongTinHLEntity> findByIdDualRead(int id) {
        if (!sqliteEnabled) {
            return trainingSessionJdbcRepository.findById(id);
        }

        // Phase 4: Primary is SQLite
        if (!fallbackToPostgres) {
            return findFromSqliteWithFallback(id);
        }

        // Phase 1-3: Primary is PostgreSQL
        Optional<ThongTinHLEntity> psql = trainingSessionJdbcRepository.findById(id);

        // If dual-write enable (Phase 2-3), validate SQLite for mismatch
        if (dualWriteEnabled) {
            try {
                validateSqliteCache(id);
            } catch (Exception ex) {
                log.warn("SQLite validation failed for id={}, error={}", id, ex.getMessage());
            }
        }

        return psql;
    }

    /**
     * Phase 4: Read from SQLite, fallback to PostgreSQL if miss
     */
    private Optional<ThongTinHLEntity> findFromSqliteWithFallback(int id) {
        try {
            Optional<TrainingSessionSqliteEntity> sqlite = trainingSessionSqliteJdbcRepository.findById(id);
            if (sqlite.isPresent()) {
                // Convert SQLite entity to PostgreSQL entity (lazy mapping)
                return Optional.of(convertFromSqlite(sqlite.get()));
            }
        } catch (Exception ex) {
            log.warn("SQLite read failed for id={}, fallback to PostgreSQL, error={}", id, ex.getMessage());
        }

        // Fallback
        return trainingSessionJdbcRepository.findById(id);
    }

    /**
     * Validate SQLite cache consistency during Phase 2-3
     */
    private void validateSqliteCache(int id) {
        Optional<TrainingSessionSqliteEntity> sqlite = trainingSessionSqliteJdbcRepository.findById(id);
        Optional<ThongTinHLEntity> psql = trainingSessionJdbcRepository.findById(id);

        if (sqlite.isPresent() && psql.isPresent()) {
            TrainingSessionSqliteEntity s = sqlite.get();
            ThongTinHLEntity p = psql.get();

            // Compare key fields
            if (!equals(s.getTrangThai(), p.getTrangThai())) {
                log.warn("SQLite mismatch: trangThai id={} sqlite={} vs postgres={}",
                        id, s.getTrangThai(), p.getTrangThai());
            }
            if (!equals(s.getCurrentEpoch(), p.getCurrentEpoch())) {
                log.warn("SQLite mismatch: currentEpoch id={} sqlite={} vs postgres={}",
                        id, s.getCurrentEpoch(), p.getCurrentEpoch());
            }
        } else if (sqlite.isEmpty() && psql.isPresent()) {
            log.warn("SQLite cache miss for id={}, exists in PostgreSQL", id);
        }
    }

    /**
     * Convert SQLite entity to  PostgreSQL entity (stub - implement full mapping)
     */
    private ThongTinHLEntity convertFromSqlite(TrainingSessionSqliteEntity sqlite) {
        ThongTinHLEntity entity = new ThongTinHLEntity();
        entity.setId(sqlite.getId());
        entity.setTrackingId(sqlite.getTrackingId());
        entity.setTrangThai(sqlite.getTrangThai());
        entity.setEpochs(sqlite.getEpochs() != null ? sqlite.getEpochs() : 0);
        entity.setBatchSize(sqlite.getBatchSize() != null ? sqlite.getBatchSize() : 0);
        entity.setLearningRate(sqlite.getLearningRate() != null ? sqlite.getLearningRate() : 0.01);
        entity.setKichThuocAnh(sqlite.getKichThuocAnh() != null ? sqlite.getKichThuocAnh() : 416);
        entity.setLoaiThietBi(sqlite.getLoaiThietBi() != null ? sqlite.getLoaiThietBi() : "cpu");
        entity.setEarlyStoppingPatience(sqlite.getEarlyStoppingPatience() != null ? sqlite.getEarlyStoppingPatience() : 5);
        entity.setOptimizer(sqlite.getOptimizer() != null ? sqlite.getOptimizer() : "Adam");
        entity.setCurrentEpoch(sqlite.getCurrentEpoch());
        entity.setDoChinhXac(sqlite.getDoChinhXac());
        entity.setDoNhay(sqlite.getDoNhay());
        entity.setBatDauLuc(sqlite.getBatDauLuc());
        entity.setKetThucLuc(sqlite.getKetThucLuc());
        entity.setDuongDanMoHinhKetQua(sqlite.getDuongDanMoHinhKetQua());
        entity.setMoHinhHLJson(sqlite.getMoHinhHLJson());
        entity.setPhienBanHLJson(sqlite.getPhienBanHLJson());
        entity.setDsMauHLJson(sqlite.getDsMauHLJson());
        return entity;
    }

    private <T> boolean equals(T a, T b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
