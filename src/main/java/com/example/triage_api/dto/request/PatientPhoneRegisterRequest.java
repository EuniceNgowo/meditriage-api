package com.example.triage_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Patient registration with phone number (no email required)")
public class PatientPhoneRegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 120, message = "Full name must not exceed 120 characters")
    @Schema(description = "Full display name", example = "Jean Mbeki", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Schema(description = "Mobile phone number", example = "+237612345678", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(description = "Password (minimum 8 characters)", example = "securePass1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "Preferred language code", example = "FR", allowableValues = {"EN", "FR"})
    private String preferredLang;
}
