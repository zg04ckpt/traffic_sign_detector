package com.trafficsigndetector.aimodelservice.service;

import com.trafficsigndetector.aimodelservice.model.CreateVersionRequest;
import com.trafficsigndetector.aimodelservice.repository.ModelJdbcRepository;
import com.trafficsigndetector.sharedmodel.MoHinh;
import com.trafficsigndetector.sharedmodel.PhienBan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ModelCatalogService {

    private final ModelJdbcRepository modelRepository;

    public ModelCatalogService(ModelJdbcRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    @Transactional(readOnly = true)
    public List<MoHinh> getAllModels() {
        return modelRepository.findAllFullOrderByIdAsc();
    }

    @Transactional
    public PhienBan createVersion(int moHinhId, CreateVersionRequest payload) {
        if (!modelRepository.moHinhExists(moHinhId)) {
            throw new IllegalArgumentException("Model not found: " + moHinhId);
        }

        String ten = payload.ten().trim();
        String moTa = payload.moTa() == null ? "" : payload.moTa().trim();
        String duongDan = payload.duongDanMH().trim();

        int versionId = modelRepository.insertPhienBan(moHinhId, ten, moTa, duongDan);
        return new PhienBan(versionId, ten, moTa, duongDan);
    }
}
