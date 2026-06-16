package com.trafficsigndetector.datasetservice.config;

import com.trafficsigndetector.datasetservice.service.DatasetCatalogService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticUploadConfig implements WebMvcConfigurer {

    private final DatasetCatalogService datasetCatalogService;

    public StaticUploadConfig(DatasetCatalogService datasetCatalogService) {
        this.datasetCatalogService = datasetCatalogService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + datasetCatalogService.getUploadRoot() + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
