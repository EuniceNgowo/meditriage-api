package com.example.triage_api.dto.response;

import com.example.triage_api.model.TriageLevel;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Data @Builder @AllArgsConstructor @NoArgsConstructor
@Schema(description = "AI-generated triage analysis for a symptom session")
public class TriageResultResponse {

    @Schema(description = "Unique result identifier")
    private UUID resultId;

    @Schema(description = "Session this result belongs to")
    private UUID sessionId;

    @Schema(description = "Triage urgency level assigned by the AI", example = "AMBER", allowableValues = {"GREEN", "AMBER", "RED"})
    private TriageLevel triageLevel;

    @Schema(description = "AI confidence score between 0.0 and 1.0", example = "0.87")
    private Double aiConfidence;

    @Schema(description = "Array of probable conditions with likelihood scores")
    private JsonNode probableConditions;

    @Schema(description = "Plain-English next-steps advice from the AI",
            example = "See a doctor within 24 hours. Monitor for worsening pain or difficulty breathing.")
    private String escalationAdvice;

    @Schema(description = "Health tips matched to the triage level")
    private List<HealthTipResponse> healthTips;

    @Schema(description = "When the triage was performed (UTC)")
    private Instant createdAt;
}
