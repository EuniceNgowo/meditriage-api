package com.example.triage_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;


@Data
@Schema(description = "Doctor rating submitted after a closed consultation")
public class RateConversationRequest {

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Schema(description = "Star rating from 1 (poor) to 5 (excellent)", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer rating;

    @Schema(description = "Optional written feedback for the doctor", example = "Very attentive and provided a thorough explanation.")
    private String feedback;
}
