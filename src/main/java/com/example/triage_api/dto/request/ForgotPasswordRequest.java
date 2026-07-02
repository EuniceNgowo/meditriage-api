package com.example.triage_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Forgot password — supply the email address registered to the account")
public class ForgotPasswordRequest {

    @NotBlank(message = "Email is required")
    @Schema(description = "Email address of the account", example = "jane@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "Account role", example = "USER", allowableValues = {"USER", "DOCTOR"})
    private String role;
}
