package com.example.triage_api.controller;

import com.example.triage_api.dto.request.UpdateUserProfileRequest;
import com.example.triage_api.dto.response.SessionResponse;
import com.example.triage_api.dto.response.UserResponse;
import com.example.triage_api.exception.ErrorResponse;
import com.example.triage_api.service.SessionService;
import com.example.triage_api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@Tag(name = "Users", description = "Patient profile and session history")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final SessionService sessionService;


    @GetMapping("/api/users/me")
    @Operation(summary = "Get the authenticated patient's profile")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile returned"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        UserResponse profile = userService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(profile);
    }


    @GetMapping("/api/users/me/sessions")
    @Operation(summary = "Get all symptom sessions for the authenticated patient")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Session list returned"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<SessionResponse>> getMySessions(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<SessionResponse> sessions = sessionService.getUserSessions(userDetails.getUsername());
        return ResponseEntity.ok(sessions);
    }


    @PutMapping("/api/users/me")
    @Operation(summary = "Update the authenticated patient's profile",
               description = "Currently supports updating the full name.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile updated"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserResponse> updateProfile(
            @Valid @RequestBody UpdateUserProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.updateProfile(userDetails.getUsername(), request));
    }


    @DeleteMapping("/api/users/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete (deactivate) the authenticated patient's own account",
               description = "Sets isActive = false. The account is retained in the database but the patient can no longer log in.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Account deactivated"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteMyAccount(
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.deactivate(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
