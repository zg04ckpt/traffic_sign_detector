package com.trafficsigndetector.datasetservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ApiError(
        @JsonProperty("timestamp") String timestamp,
        @JsonProperty("status") int status,
        @JsonProperty("error") String error,
        @JsonProperty("message") String message,
        @JsonProperty("path") String path
) {
}
