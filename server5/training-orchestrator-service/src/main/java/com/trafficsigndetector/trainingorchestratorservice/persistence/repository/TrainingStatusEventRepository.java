package com.trafficsigndetector.trainingorchestratorservice.persistence.repository;

import com.trafficsigndetector.trainingorchestratorservice.persistence.entity.TrainingStatusEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrainingStatusEventRepository extends JpaRepository<TrainingStatusEventEntity, Long> {

    Optional<TrainingStatusEventEntity> findTopByTrackingIdOrderByUpdatedAtDescIdDesc(String trackingId);

    List<TrainingStatusEventEntity> findByTrackingIdOrderByUpdatedAtAscIdAsc(String trackingId);
}
