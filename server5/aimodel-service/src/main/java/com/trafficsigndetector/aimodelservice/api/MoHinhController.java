package com.trafficsigndetector.aimodelservice.api;

import com.trafficsigndetector.aimodelservice.model.CreateVersionRequest;
import com.trafficsigndetector.aimodelservice.service.ModelCatalogService;
import com.trafficsigndetector.sharedmodel.MoHinh;
import com.trafficsigndetector.sharedmodel.PhienBan;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class MoHinhController {

    private final ModelCatalogService modelCatalogService;

    public MoHinhController(ModelCatalogService modelCatalogService) {
        this.modelCatalogService = modelCatalogService;
    }

    @GetMapping("/mo-hinh")
    public List<MoHinh> getMoHinh() {
        return modelCatalogService.getAllModels();
    }

    @PostMapping("/mo-hinh/{moHinhId}/phien-ban")
    @ResponseStatus(HttpStatus.CREATED)
    public PhienBan createNewVersion(
            @PathVariable int moHinhId,
            @Valid @RequestBody CreateVersionRequest payload
    ) {
        try {
            return modelCatalogService.createVersion(moHinhId, payload);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }
}
