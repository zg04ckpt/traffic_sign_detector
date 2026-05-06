package com.trafficsigndetector.client2.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@RestController
@RequestMapping("/client2/trainings")
public class TrainingProxyController {

    private final RestClient gatewayRestClient;
    private final ObjectMapper objectMapper;

    public TrainingProxyController(RestClient gatewayRestClient, ObjectMapper objectMapper) {
        this.gatewayRestClient = gatewayRestClient;
        this.objectMapper = objectMapper;
    }

    @GetMapping(value = "/models", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getModels() {
        return gatewayRestClient.get()
                .uri("/api/mo-hinh")
                .retrieve()
                .toEntity(String.class);
    }

    @GetMapping(value = "/datasets", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getDatasets() throws Exception {
        ResponseEntity<String> upstream = gatewayRestClient.get()
                .uri("/api/tap-du-lieu")
                .retrieve()
                .toEntity(String.class);

        String body = upstream.getBody();
        if (body == null || body.isBlank()) {
            return upstream;
        }

        JsonNode root = objectMapper.readTree(body);
        if (!(root instanceof ArrayNode datasets)) {
            return upstream;
        }

        for (JsonNode datasetNode : datasets) {
            JsonNode samples = datasetNode.get("DsMau");
            if (!(samples instanceof ArrayNode sampleArray)) {
                continue;
            }
            for (JsonNode sampleNode : sampleArray) {
                JsonNode pathNode = sampleNode.get("DuongDanAnh");
                if (pathNode == null || !pathNode.isTextual()) {
                    continue;
                }
                String path = pathNode.asText();
                if (!path.startsWith("http://") && !path.startsWith("https://")) {
                    String normalized = path.startsWith("/") ? path : "/" + path;
                    ((com.fasterxml.jackson.databind.node.ObjectNode) sampleNode)
                            .put("DuongDanAnh", "http://localhost:8081" + normalized);
                }
            }
        }

        return ResponseEntity.status(upstream.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(datasets));
    }

        @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<String> createTraining(@RequestBody Map<String, Object> payload) {
        return gatewayRestClient.post()
            .uri("/api/huan-luyen")
            .contentType(MediaType.APPLICATION_JSON)
            .body(payload)
            .retrieve()
            .toEntity(String.class);
        }

        @PostMapping(value = "/{thongTinHLId}/start", produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<String> startTraining(@PathVariable int thongTinHLId) {
        return gatewayRestClient.post()
            .uri("/api/huan-luyen/{thongTinHLId}/bat-dau", thongTinHLId)
            .retrieve()
            .toEntity(String.class);
        }

        @GetMapping(value = "/{thongTinHLId}/result", produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<String> getTrainingResult(@PathVariable int thongTinHLId) {
        return gatewayRestClient.get()
            .uri("/api/huan-luyen/{thongTinHLId}/ket-qua", thongTinHLId)
            .retrieve()
            .toEntity(String.class);
        }

        @PostMapping(value = "/{thongTinHLId}/save-version", produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<String> saveTrainingVersion(@PathVariable int thongTinHLId) {
        return gatewayRestClient.post()
            .uri("/api/huan-luyen/{thongTinHLId}/luu-phien-ban", thongTinHLId)
            .retrieve()
            .toEntity(String.class);
        }

    @PostMapping(value = "/{trackingId}/enqueue", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> enqueueTraining(
            @PathVariable String trackingId,
            @RequestParam(defaultValue = "traffic-sign-default") String modelCode
    ) {
        return gatewayRestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/training-orchestrator/api/trainings/{trackingId}/enqueue")
                        .queryParam("modelCode", modelCode)
                        .build(trackingId))
                .retrieve()
                .toEntity(String.class);
    }

    @GetMapping(value = "/{trackingId}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getLatestStatus(@PathVariable String trackingId) {
        return gatewayRestClient.get()
                .uri("/training-orchestrator/api/trainings/{trackingId}/status", trackingId)
                .retrieve()
                .toEntity(String.class);
    }

    @GetMapping(value = "/{trackingId}/events", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTimeline(@PathVariable String trackingId) {
        return gatewayRestClient.get()
                .uri("/training-orchestrator/api/trainings/{trackingId}/events", trackingId)
                .retrieve()
                .toEntity(String.class);
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<String> handleGatewayError(RestClientResponseException ex) {
        HttpStatusCode status = HttpStatusCode.valueOf(ex.getStatusCode().value());
        String body = ex.getResponseBodyAsString();
        if (body == null || body.isBlank()) {
            body = "{\"error\":\"Gateway request failed\"}";
        }
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<String> handleGatewayConnectionError(RestClientException ex) {
        String body = "{\"error\":\"Upstream service unavailable\",\"detail\":\"Cannot connect to API Gateway at http://localhost:8081\"}";
        return ResponseEntity.status(503)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }
}
