package com.trafficsigndetector.trainingorchestratorservice.persistence.sqlite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainingSessionSqliteRepository extends JpaRepository<TrainingSessionSqliteEntity, Integer> {
    Optional<TrainingSessionSqliteEntity> findByTrackingId(String trackingId);
}
