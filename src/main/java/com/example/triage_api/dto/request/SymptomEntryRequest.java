package com.example.triage_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
@Schema(description = "A single patient-reported symptom")
public class SymptomEntryRequest {

    @NotBlank(message = "Symptom description is required")
    @Schema(description = "Plain-English description of the symptom", example = "Sharp chest pain when breathing deeply", requiredMode = Schema.RequiredMode.REQUIRED)
    private String symptomText;

    @Min(value = 1, message = "Severity must be at least 1")
    @Max(value = 10, message = "Severity must be at most 10")
    @Schema(description = "Severity on a scale of 1 (very mild) to 10 (unbearable)", example = "7")
    private Integer severity;

    @Schema(description = "How long the symptom has been present", example = "Since yesterday morning")
    private String durationText;
}
