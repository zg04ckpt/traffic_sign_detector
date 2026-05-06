package com.trafficsigndetector.trainingorchestratorservice.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "training_status_event")
public class TrainingStatusEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Integer sessionId;

    @Column(name = "tracking_id", nullable = false, length = 64)
    private String trackingId;

    @Column(name = "state", nullable = false, length = 32)
    private String state;

    @Column(name = "detail", columnDefinition = "text")
    private String detail;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "current_epoch")
    private Integer currentEpoch;

    @Column(name = "precision")
    private Double precision;

    @Column(name = "recall")
    private Double recall;

    @Column(name = "log_line", columnDefinition = "text")
    private String logLine;

    @Column(name = "model_artifact_path", columnDefinition = "text")
    private String modelArtifactPath;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSessionId() {
        return sessionId;
    }

    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getCurrentEpoch() {
        return currentEpoch;
    }

    public void setCurrentEpoch(Integer currentEpoch) {
        this.currentEpoch = currentEpoch;
    }

    public Double getPrecision() {
        return precision;
    }

    public void setPrecision(Double precision) {
        this.precision = precision;
    }

    public Double getRecall() {
        return recall;
    }

    public void setRecall(Double recall) {
        this.recall = recall;
    }

    public String getLogLine() {
        return logLine;
    }

    public void setLogLine(String logLine) {
        this.logLine = logLine;
    }

    public String getModelArtifactPath() {
        return modelArtifactPath;
    }

    public void setModelArtifactPath(String modelArtifactPath) {
        this.modelArtifactPath = modelArtifactPath;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
