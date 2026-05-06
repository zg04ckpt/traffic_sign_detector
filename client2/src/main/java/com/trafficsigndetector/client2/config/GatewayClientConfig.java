package com.trafficsigndetector.client2.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GatewayClientConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    public RestClient gatewayRestClient(@Value("${client2.gateway-base-url}") String gatewayBaseUrl) {
        return RestClient.builder()
                .baseUrl(gatewayBaseUrl)
                .build();
    }
}
