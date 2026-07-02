package com.example.triage_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data @Builder @AllArgsConstructor @NoArgsConstructor
@Schema(description = "Authentication token returned after a successful login")
public class JwtResponse {

    @Schema(description = "JWT bearer token to include in the Authorization header", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Token type — always 'Bearer'", example = "Bearer")
    private String type;

    @Schema(description = "Email address of the authenticated account", example = "jane@example.com")
    private String email;

    @Schema(description = "Full display name of the authenticated account", example = "Jane Doe")
    private String fullName;

    @Schema(description = "Role of the authenticated account", example = "USER", allowableValues = {"USER", "DOCTOR"})
    private String role;

    @Schema(description = "Doctor ID — only present when role is DOCTOR")
    private java.util.UUID doctorId;

    @Schema(description = "Phone number — present when the account was registered with phone instead of email")
    private String phoneNumber;
}
