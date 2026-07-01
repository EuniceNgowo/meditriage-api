package com.example.triage_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;


@Data @Builder @AllArgsConstructor @NoArgsConstructor
@Schema(description = "Doctor public profile")
public class DoctorResponse {

    @Schema(description = "Unique doctor identifier")
    private UUID doctorId;

    @Schema(description = "Full name", example = "Dr. Amina Bello")
    private String fullName;

    @Schema(description = "Medical specialty", example = "General Practice")
    private String specialty;

    @Schema(description = "Medical license number", example = "MD-2024-00123")
    private String licenseNumber;

    @Schema(description = "Professional bio", example = "10 years in emergency medicine")
    private String bio;

    @Schema(description = "Years of professional experience", example = "8")
    private Integer yearsExperience;

    @Schema(description = "Comma-separated language codes the doctor speaks", example = "EN,FR")
    private String languagesSpoken;

    @Schema(description = "Current availability status", example = "AVAILABLE", allowableValues = {"AVAILABLE", "BUSY", "OFFLINE"})
    private String status;

    @Schema(description = "Average star rating (1–5), null if not yet rated", example = "4.7")
    private Double ratingAverage;

    @Schema(description = "Total number of ratings received", example = "23")
    private Integer ratingCount;
}
