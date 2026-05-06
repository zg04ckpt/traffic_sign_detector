package com.trafficsigndetector.datasetservice.repository;

import com.trafficsigndetector.datasetservice.entity.TapDuLieuEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TapDuLieuRepository extends JpaRepository<TapDuLieuEntity, Integer> {

    @EntityGraph(attributePaths = {"dsMau"})
    List<TapDuLieuEntity> findAllByOrderByIdAsc();
}
