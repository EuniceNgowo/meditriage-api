package com.example.triage_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Reset password using the OTP received by email")
public class ResetPasswordRequest {

    @NotBlank(message = "Email is required")
    @Schema(description = "Email address of the account", example = "jane@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "OTP code is required")
    @Size(min = 6, max = 6, message = "OTP must be exactly 6 digits")
    @Schema(description = "6-digit code received by email", example = "482910", requiredMode = Schema.RequiredMode.REQUIRED)
    private String otp;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(description = "New password", example = "newSecurePass1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;

    @Schema(description = "Account role", example = "USER", allowableValues = {"USER", "DOCTOR"})
    private String role;
}
