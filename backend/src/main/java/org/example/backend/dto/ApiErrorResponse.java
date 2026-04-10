package org.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Standard error response returned by the API.")
public record ApiErrorResponse(
        @Schema(description = "HTTP status code", example = "400")
        int status,

        @Schema(description = "HTTP reason phrase", example = "Bad Request")
        String error,

        @Schema(description = "Human-readable message", example = "Validation failed")
        String message,

        @Schema(description = "Request path that caused the error", example = "/api/loan-applications")
        String path,

        @Schema(description = "Field-level validation errors")
        Map<String, String> fieldErrors
) {}