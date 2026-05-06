package com.trafficsigndetector.aimodelservice.repository;

import com.trafficsigndetector.aimodelservice.entity.MoHinhEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MoHinhRepository extends JpaRepository<MoHinhEntity, Integer> {

    @EntityGraph(attributePaths = "dsPhienBan")
    List<MoHinhEntity> findAllByOrderByIdAsc();
}
