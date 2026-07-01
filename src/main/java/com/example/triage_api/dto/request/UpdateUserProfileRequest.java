package com.example.triage_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Fields a patient can update on their own profile")
public class UpdateUserProfileRequest {

    @Size(min = 2, max = 120, message = "Name must be between 2 and 120 characters")
    @Schema(description = "Full name", example = "Jean Dupont")
    private String fullName;
}
