package com.example.triage_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Fields a doctor can update on their own profile (all fields optional — only non-null values are applied)")
public class UpdateDoctorProfileRequest {

    @Size(min = 2, max = 150, message = "Name must be between 2 and 150 characters")
    @Schema(description = "Full name", example = "Dr. Amina Bello")
    private String fullName;

    @Size(max = 120, message = "Specialty must be at most 120 characters")
    @Schema(description = "Medical specialty", example = "Cardiology")
    private String specialty;

    @Schema(description = "Short biography visible to patients", example = "10 years of experience in family medicine.")
    private String bio;

    @Schema(description = "Languages spoken, comma-separated ISO codes", example = "EN,FR")
    private String languagesSpoken;

    @Schema(description = "Years of clinical experience", example = "10")
    private Integer yearsExperience;
}
