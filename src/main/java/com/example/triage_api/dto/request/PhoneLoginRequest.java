package com.example.triage_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Phone number + password login credentials")
public class PhoneLoginRequest {

    @NotBlank(message = "Phone number is required")
    @Schema(description = "Registered phone number", example = "+237612345678", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Schema(description = "Account password", example = "securePass1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
