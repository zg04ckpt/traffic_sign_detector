package com.trafficsigndetector.trainingorchestratorservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trafficsigndetector.trainingorchestratorservice.messaging.TrainingSessionEventMessage;
import com.trafficsigndetector.trainingorchestratorservice.persistence.entity.ThongTinHLEntity;
import com.trafficsigndetector.trainingorchestratorservice.persistence.sqlite.TrainingSessionSqliteEntity;
import com.trafficsigndetector.trainingorchestratorservice.persistence.sqlite.TrainingSessionSqliteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Sync từ PostgreSQL → SQLite via RabbitMQ events.
 * 
 * Tuần 1: Shadow read (không được dùng nhưng được sync)
 * Tuần 2-3: Dual-write (ghi 100%, sync validate)
 * Tuần 4: Primary read (chính thức enable, PostgreSQL là fallback)
 */
@Service
@Slf4j
public class TrainingSessionSyncService {

    private final TrainingSessionSqliteRepository sqliteRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.migration.sqlite-enabled:false}")
    private boolean sqliteEnabled;

    public TrainingSessionSyncService(
            TrainingSessionSqliteRepository sqliteRepository,
            ObjectMapper objectMapper) {
        this.sqliteRepository = sqliteRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Listen training events và sync vào SQLite
     */
    @RabbitListener(queues = "${app.rabbitmq.queue.training-status:training.status.queue}")
    @Transactional
    public void onTrainingEvent(String message) {
        if (!sqliteEnabled) {
            log.debug("SQLite sync disabled, skipping event");
            return;
        }

        try {
            TrainingSessionEventMessage event = objectMapper.readValue(message, TrainingSessionEventMessage.class);
            if (event.id() == null) {
                log.warn("Training event has no id, skipping");
                return;
            }
            Integer sessionId = event.id();
            String trackingId = event.trackingId();
            String eventType = event.eventType();

            log.debug("Received training event: id={}, type={}", sessionId, eventType);

            // Update SQLite cache
            Optional<TrainingSessionSqliteEntity> existing = sqliteRepository.findById(sessionId);
            if (existing.isEmpty()) {
                log.warn("SQLite record not found for id={}, creating new", sessionId);
            }

            TrainingSessionSqliteEntity cache = existing.orElseGet(() -> 
                    TrainingSessionSqliteEntity.builder()
                            .id(sessionId)
                            .trackingId(trackingId)
                            .build());

            // Update fields from event
            cache.setTrangThai(event.trangThai());
            cache.setCurrentEpoch(event.currentEpoch() == null ? 0 : event.currentEpoch());
            cache.setDoChinhXac(event.doChinhXac());
            cache.setDoNhay(event.doNhay());
            cache.setSyncedAt(Instant.now());
            cache.setSyncVersion((cache.getSyncVersion() == null ? 0L : cache.getSyncVersion()) + 1);

            TrainingSessionSqliteEntity saved = sqliteRepository.save(cache);
            log.debug("Synced training to SQLite: id={}, version={}", saved.getId(), saved.getSyncVersion());

        } catch (Exception ex) {
            log.error("Failed to sync training event to SQLite: error={}", ex.getMessage(), ex);
        }
    }

    /**
     * Publish event khi training session created di PostgreSQL
     * Gọi từ TrainingSessionService.createSession()
     */
    public void syncFromPostgres(ThongTinHLEntity postgres) {
        if (!sqliteEnabled) {
            return;
        }

        try {
            TrainingSessionSqliteEntity cache = TrainingSessionSqliteEntity.builder()
                    .id(postgres.getId())
                    .trackingId(postgres.getTrackingId())
                    .trangThai(postgres.getTrangThai())
                    .epochs(postgres.getEpochs())
                    .batchSize(postgres.getBatchSize())
                    .learningRate(postgres.getLearningRate())
                    .kichThuocAnh(postgres.getKichThuocAnh())
                    .loaiThietBi(postgres.getLoaiThietBi())
                    .earlyStoppingPatience(postgres.getEarlyStoppingPatience())
                    .optimizer(postgres.getOptimizer())
                    .currentEpoch(postgres.getCurrentEpoch())
                    .doChinhXac(postgres.getDoChinhXac())
                    .doNhay(postgres.getDoNhay())
                    .batDauLuc(postgres.getBatDauLuc())
                    .ketThucLuc(postgres.getKetThucLuc())
                    .duongDanMoHinhKetQua(postgres.getDuongDanMoHinhKetQua())
                    .moHinhHLJson(postgres.getMoHinhHLJson())
                    .phienBanHLJson(postgres.getPhienBanHLJson())
                    .dsMauHLJson(postgres.getDsMauHLJson())
                    .syncedAt(Instant.now())
                    .syncVersion(0L)
                    .build();

            sqliteRepository.save(cache);
            log.debug("Synced training to SQLite from PostgreSQL insert: id={}", postgres.getId());
        } catch (Exception ex) {
            log.error("Failed to sync training to SQLite from PostgreSQL: id={}, error={}", 
                    postgres.getId(), ex.getMessage(), ex);
        }
    }
}
