package com.example.triage_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.UUID;


@Data @Builder @AllArgsConstructor @NoArgsConstructor
@Schema(description = "A single recorded symptom entry")
public class SymptomEntryResponse {

    @Schema(description = "Unique entry identifier")
    private UUID entryId;

    @Schema(description = "Plain-English description of the symptom", example = "Sharp chest pain when breathing deeply")
    private String symptomText;

    @Schema(description = "Severity on a scale of 1 (very mild) to 10 (unbearable)", example = "7")
    private Integer severity;

    @Schema(description = "How long the symptom has been present", example = "Since yesterday morning")
    private String durationText;

    @Schema(description = "When this entry was recorded (UTC)")
    private Instant recordedAt;
}
