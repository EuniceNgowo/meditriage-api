package com.example.triage_api.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Standard error response body")
public class ErrorResponse {

    @Schema(description = "When the error occurred (UTC)")
    private Instant timestamp;

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "Short error category", example = "Not Found")
    private String error;

    @Schema(description = "Human-readable error detail", example = "Doctor not found with id: 3fa85f64-...")
    private String message;
}
