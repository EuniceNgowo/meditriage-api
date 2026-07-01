package com.example.triage_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;


@Data
@Schema(description = "Doctor registration payload")
public class DoctorRegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must not exceed 150 characters")
    @Schema(description = "Full name as it appears on the medical license", example = "Dr. Amina Bello", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Schema(description = "Work email address", example = "dr.bello@hospital.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(description = "Password (minimum 8 characters)", example = "secure1234", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "Specialty is required")
    @Schema(description = "Medical specialty", example = "General Practice", requiredMode = Schema.RequiredMode.REQUIRED)
    private String specialty;

    @NotBlank(message = "License number is required")
    @Schema(description = "Medical license number", example = "MD-2024-00123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String licenseNumber;

    @Schema(description = "Short professional bio", example = "10 years in emergency medicine specialising in trauma care.")
    private String bio;

    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 60, message = "Years of experience seems too high")
    @Schema(description = "Years of professional medical experience", example = "8")
    private Integer yearsExperience;

    @Schema(description = "Comma-separated language codes the doctor speaks", example = "EN,FR")
    private String languagesSpoken;
}
