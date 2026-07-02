package com.example.triage_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.UUID;


@Data @Builder @AllArgsConstructor @NoArgsConstructor
@Schema(description = "Patient account profile")
public class UserResponse {

    @Schema(description = "Unique patient identifier")
    private UUID userId;

    @Schema(description = "Full display name", example = "Jane Doe")
    private String fullName;

    @Schema(description = "Email address", example = "jane@example.com")
    private String email;

    @Schema(description = "Preferred language code", example = "EN", allowableValues = {"EN", "FR"})
    private String preferredLang;

    @Schema(description = "Account creation timestamp (UTC)")
    private Instant createdAt;

    @Schema(description = "Whether the account is active")
    private Boolean isActive;
}
