package com.example.triage_api.controller;

import com.example.triage_api.dto.request.DoctorRegisterRequest;
import com.example.triage_api.dto.request.LoginRequest;
import com.example.triage_api.dto.request.UpdateDoctorProfileRequest;
import com.example.triage_api.dto.response.DoctorResponse;
import com.example.triage_api.dto.response.JwtResponse;
import com.example.triage_api.exception.ErrorResponse;
import com.example.triage_api.model.DoctorStatus;
import com.example.triage_api.service.AuthService;
import com.example.triage_api.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctors", description = "Doctor profiles, availability, and registration")
public class DoctorController {

    private final DoctorService doctorService;
    private final AuthService authService;

    // ─── Public endpoints ─────────────────────────────────────────────────


    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new doctor account",
               description = "Creates a doctor account. The doctor starts as OFFLINE and must set their status to AVAILABLE via `PUT /api/doctors/me/status`.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Doctor account created",
                     content = @Content(schema = @Schema(implementation = JwtResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email address already registered",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<JwtResponse> register(
            @Valid @RequestBody DoctorRegisterRequest request) {

        JwtResponse response = doctorService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PostMapping("/login")
    @Operation(summary = "Doctor login",
               description = "Authenticate with email and password. Copy the returned `token` and use it as `Authorization: Bearer <token>` on all protected endpoints.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful — JWT token returned",
                     content = @Content(schema = @Schema(implementation = JwtResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid email or password",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.doctorLogin(request));
    }


    @GetMapping
    @Operation(summary = "List active doctors, optionally filtered by specialty",
               description = "Returns a paginated list of all active doctors. Use the `specialty` parameter to filter by specialty (case-insensitive partial match). Sorted by rating by default.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Doctor list returned")
    })
    public ResponseEntity<Page<DoctorResponse>> getAllDoctors(
            @Parameter(description = "Partial specialty name to filter by (e.g. 'cardio')") @RequestParam(required = false) String specialty,
            @PageableDefault(size = 20, sort = "ratingAverage", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<DoctorResponse> doctors = specialty != null && !specialty.isBlank()
                ? doctorService.getDoctorsBySpecialty(specialty, pageable)
                : doctorService.getAllDoctors(pageable);

        return ResponseEntity.ok(doctors);
    }


    @GetMapping("/available")
    @Operation(summary = "List only currently available doctors",
               description = "Returns doctors whose status is AVAILABLE, sorted by rating.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Available doctor list returned")
    })
    public ResponseEntity<Page<DoctorResponse>> getAvailableDoctors(
            @PageableDefault(size = 20, sort = "ratingAverage", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(doctorService.getAvailableDoctors(pageable));
    }


    @GetMapping("/{id}")
    @Operation(summary = "Get a specific doctor's public profile")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Doctor profile returned",
                     content = @Content(schema = @Schema(implementation = DoctorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Doctor not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DoctorResponse> getDoctorById(
            @Parameter(description = "Doctor UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    // ─── Protected endpoints (doctor must be logged in) ───────────────────


    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get the authenticated doctor's own profile")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile returned",
                     content = @Content(schema = @Schema(implementation = DoctorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DoctorResponse> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(doctorService.getMyProfile(userDetails.getUsername()));
    }


    @PutMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update the authenticated doctor's profile",
               description = "Only non-null fields are updated. Name, specialty, bio, languages spoken, and years of experience can be changed.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile updated",
                     content = @Content(schema = @Schema(implementation = DoctorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DoctorResponse> updateProfile(
            @Valid @RequestBody UpdateDoctorProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(doctorService.updateProfile(userDetails.getUsername(), request));
    }


    @DeleteMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete (deactivate) the authenticated doctor's own account",
               description = "Sets isActive = false and status = OFFLINE. The account and data are retained but the doctor can no longer log in or appear in searches.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Account deactivated"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteMyAccount(
            @AuthenticationPrincipal UserDetails userDetails) {
        doctorService.deactivate(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Deactivate any doctor by ID (admin use)",
               description = "Sets the doctor's isActive = false. Requires a valid JWT. Intended for administrative use via Swagger UI.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Doctor deactivated"),
        @ApiResponse(responseCode = "404", description = "Doctor not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deactivateDoctorById(
            @Parameter(description = "Doctor UUID", required = true) @PathVariable UUID id) {
        doctorService.deactivateById(id);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/me/status")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update availability status",
               description = "Set to AVAILABLE to accept consultations, or OFFLINE to stop receiving requests. BUSY is managed automatically based on active chat count.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status updated",
                     content = @Content(schema = @Schema(implementation = DoctorResponse.class))),
        @ApiResponse(responseCode = "400", description = "BUSY status cannot be set manually",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DoctorResponse> updateStatus(
            @Parameter(description = "New status — AVAILABLE or OFFLINE", required = true)
            @RequestParam DoctorStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (status == DoctorStatus.BUSY) {
            throw new com.example.triage_api.exception.BadRequestException(
                    "BUSY status is managed automatically. Set yourself to AVAILABLE or OFFLINE.");
        }

        DoctorResponse updated = doctorService.updateStatus(userDetails.getUsername(), status);
        return ResponseEntity.ok(updated);
    }
}
