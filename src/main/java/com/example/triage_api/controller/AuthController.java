package com.example.triage_api.controller;

import com.example.triage_api.dto.request.*;
import com.example.triage_api.dto.response.JwtResponse;
import com.example.triage_api.dto.response.MessageResponse;
import com.example.triage_api.exception.ErrorResponse;
import com.example.triage_api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and log in as a patient (email or phone number)")
public class AuthController {

    private final AuthService authService;

    // ─── Email register ────────────────────────────────────────────────────

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new patient account (email)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Account created successfully",
                     content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error or duplicate email",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    // ─── Phone register ────────────────────────────────────────────────────

    @PostMapping("/register-phone")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new patient account (phone number — no email required)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Account created successfully",
                     content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error or duplicate phone",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MessageResponse> registerByPhone(@Valid @RequestBody PatientPhoneRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerByPhone(request));
    }

    // ─── Email login ───────────────────────────────────────────────────────

    @PostMapping("/login")
    @Operation(summary = "Patient login (email + password)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "JWT token returned",
                     content = @Content(schema = @Schema(implementation = JwtResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // ─── Phone login ───────────────────────────────────────────────────────

    @PostMapping("/login-phone")
    @Operation(summary = "Patient login (phone number + password)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "JWT token returned",
                     content = @Content(schema = @Schema(implementation = JwtResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<JwtResponse> loginByPhone(@Valid @RequestBody PhoneLoginRequest request) {
        return ResponseEntity.ok(authService.loginByPhone(request));
    }

    // ─── Doctor email login (legacy compatibility path) ────────────────────

    @PostMapping("/doctor/login")
    @Operation(summary = "Doctor login (email) — prefer /api/doctors/login")
    public ResponseEntity<JwtResponse> doctorLogin(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.doctorLogin(request));
    }

    // ─── Forgot password ───────────────────────────────────────────────────

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a 6-digit password-reset OTP",
               description = "Sends a code to the registered email. Expires in 15 minutes. Set role=DOCTOR for doctor accounts.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OTP email sent",
                     content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "404", description = "No account with this email",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    // ─── Reset password ────────────────────────────────────────────────────

    @PostMapping("/reset-password")
    @Operation(summary = "Confirm password reset with OTP",
               description = "Verifies the 6-digit code and sets the new password. Set role=DOCTOR for doctor accounts.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password updated",
                     content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid or expired OTP",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}
