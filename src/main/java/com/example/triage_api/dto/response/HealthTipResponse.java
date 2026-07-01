package com.example.triage_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;


@Data @Builder @AllArgsConstructor @NoArgsConstructor
@Schema(description = "A health tip matched to the patient's triage level")
public class HealthTipResponse {

    @Schema(description = "Unique tip identifier")
    private UUID tipId;

    @Schema(description = "Short title of the tip", example = "Stay Hydrated")
    private String title;

    @Schema(description = "Full tip content", example = "Drink at least 8 glasses of water per day and avoid caffeine.")
    private String content;

    @Schema(description = "Tip category", example = "hydration", allowableValues = {"hydration", "rest", "medication", "emergency", "monitoring"})
    private String category;
}
