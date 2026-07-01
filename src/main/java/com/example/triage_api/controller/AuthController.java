package com.example.triage_api.controller;

import com.example.triage_api.dto.request.LoginRequest;
import com.example.triage_api.dto.request.RegisterRequest;
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
@Tag(name = "Authentication", description = "Register a new patient account and log in to get a JWT token")
public class AuthController {

    private final AuthService authService;


    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new patient account",
               description = "Creates a new patient account. Returns a 409 if the email is already registered.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Account created successfully",
                     content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error (e.g. missing field, email format)",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email address already registered",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MessageResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        MessageResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PostMapping("/login")
    @Operation(summary = "Log in and receive a JWT token",
               description = "Authenticate with email and password. Copy the returned `token` and pass it as `Authorization: Bearer <token>` on all protected endpoints.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful — JWT token returned",
                     content = @Content(schema = @Schema(implementation = JwtResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid email or password",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<JwtResponse> login(
            @Valid @RequestBody LoginRequest request) {

        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/doctor/login")
    @Operation(summary = "Doctor login",
               description = "Authenticate as a doctor. Returns a JWT token and the doctor's ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful — JWT token returned",
                     content = @Content(schema = @Schema(implementation = JwtResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid email or password",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<JwtResponse> doctorLogin(
            @Valid @RequestBody LoginRequest request) {

        JwtResponse response = authService.doctorLogin(request);
        return ResponseEntity.ok(response);
    }
}
