package com.trafficsigndetector.trainingorchestratorservice.persistence.jdbc;

import com.trafficsigndetector.trainingorchestratorservice.persistence.entity.TrainingStatusEventEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainingStatusEventJdbcRepository {

    private final JdbcTemplate jdbc;

    public TrainingStatusEventJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public TrainingStatusEventEntity save(TrainingStatusEventEntity event) {
        Instant created = event.getCreatedAt() != null ? event.getCreatedAt() : Instant.now();
        Long id = jdbc.queryForObject(
                """
                        INSERT INTO training_status_event (
                            session_id, tracking_id, state, detail, updated_at, current_epoch,
                            "precision", "recall", log_line, model_artifact_path, created_at
                        ) VALUES (?,?,?,?,?,?,?,?,?,?,?)
                        RETURNING id
                        """,
                Long.class,
                event.getSessionId(),
                event.getTrackingId(),
                event.getState(),
                event.getDetail(),
                Timestamp.from(event.getUpdatedAt()),
                event.getCurrentEpoch(),
                event.getPrecision(),
                event.getRecall(),
                event.getLogLine(),
                event.getModelArtifactPath(),
                Timestamp.from(created)
        );
        event.setId(id);
        event.setCreatedAt(created);
        return event;
    }

    public Optional<TrainingStatusEventEntity> findTopByTrackingIdOrderByUpdatedAtDescIdDesc(String trackingId) {
        List<TrainingStatusEventEntity> rows = jdbc.query(
                """
                        SELECT id, session_id, tracking_id, state, detail, updated_at, current_epoch,
                               "precision" AS prec, "recall" AS rec, log_line, model_artifact_path, created_at
                        FROM training_status_event
                        WHERE tracking_id = ?
                        ORDER BY updated_at DESC, id DESC
                        LIMIT 1
                        """,
                (rs, rowNum) -> mapRow(rs),
                trackingId
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public List<TrainingStatusEventEntity> findByTrackingIdOrderByUpdatedAtAscIdAsc(String trackingId) {
        return jdbc.query(
                """
                        SELECT id, session_id, tracking_id, state, detail, updated_at, current_epoch,
                               "precision" AS prec, "recall" AS rec, log_line, model_artifact_path, created_at
                        FROM training_status_event
                        WHERE tracking_id = ?
                        ORDER BY updated_at ASC, id ASC
                        """,
                (rs, rowNum) -> mapRow(rs),
                trackingId
        );
    }

    private static TrainingStatusEventEntity mapRow(ResultSet rs) throws SQLException {
        TrainingStatusEventEntity e = new TrainingStatusEventEntity();
        e.setId(rs.getLong("id"));
        e.setSessionId(rs.getInt("session_id"));
        e.setTrackingId(rs.getString("tracking_id"));
        e.setState(rs.getString("state"));
        e.setDetail(rs.getString("detail"));
        Timestamp ua = rs.getTimestamp("updated_at");
        e.setUpdatedAt(ua != null ? ua.toInstant() : null);
        int ce = rs.getInt("current_epoch");
        e.setCurrentEpoch(rs.wasNull() ? null : ce);
        double p = rs.getDouble("prec");
        e.setPrecision(rs.wasNull() ? null : p);
        double r = rs.getDouble("rec");
        e.setRecall(rs.wasNull() ? null : r);
        e.setLogLine(rs.getString("log_line"));
        e.setModelArtifactPath(rs.getString("model_artifact_path"));
        Timestamp ca = rs.getTimestamp("created_at");
        e.setCreatedAt(ca != null ? ca.toInstant() : null);
        return e;
    }
}
