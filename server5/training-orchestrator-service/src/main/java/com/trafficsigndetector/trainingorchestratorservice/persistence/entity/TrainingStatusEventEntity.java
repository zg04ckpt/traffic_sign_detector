package com.trafficsigndetector.trainingorchestratorservice.persistence.entity;

import java.time.Instant;

/**
 * Timeline row for {@code training_status_event}. Plain bean — no JPA.
 */
public class TrainingStatusEventEntity {

    private Long id;

    private Integer sessionId;

    private String trackingId;

    private String state;

    private String detail;

    private Instant updatedAt;

    private Integer currentEpoch;

    private Double precision;

    private Double recall;

    private String logLine;

    private String modelArtifactPath;

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
