package com.trafficsigndetector.trainingorchestratorservice.persistence.repository;

import com.trafficsigndetector.trainingorchestratorservice.persistence.entity.MauHLEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MauHLRepository extends JpaRepository<MauHLEntity, Integer> {
    List<MauHLEntity> findByThongTinHLIdOrderByIdAsc(Integer thongTinHLId);

    void deleteByThongTinHLId(Integer thongTinHLId);
}
