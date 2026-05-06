package com.trafficsigndetector.trainingorchestratorservice.persistence.repository;

import com.trafficsigndetector.trainingorchestratorservice.persistence.entity.ThongTinHLEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ThongTinHLRepository extends JpaRepository<ThongTinHLEntity, Integer> {
    Optional<ThongTinHLEntity> findByTrackingId(String trackingId);

    long countByTrangThai(String trangThai);

    long countByTrangThaiIn(List<String> trangThai);
}
