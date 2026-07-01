package com.example.triage_api.controller;

import com.example.triage_api.dto.request.SymptomEntryRequest;
import com.example.triage_api.dto.response.SessionResponse;
import com.example.triage_api.dto.response.SymptomEntryResponse;
import com.example.triage_api.dto.response.TriageResultResponse;
import com.example.triage_api.exception.ErrorResponse;
import com.example.triage_api.service.SessionService;
import com.example.triage_api.service.TriageService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "Symptom Sessions", description = "Manage triage sessions, symptom entries, and AI analysis")
@SecurityRequirement(name = "bearerAuth")
public class SessionController {

    private final SessionService sessionService;
    private final TriageService triageService;


    @PostMapping
    @Operation(summary = "Start a new symptom session",
               description = "Opens a new session for the authenticated patient. Add symptoms via `POST /api/sessions/{id}/entries`, then trigger analysis via `POST /api/sessions/{id}/triage`.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Session created",
                     content = @Content(schema = @Schema(implementation = SessionResponse.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SessionResponse> createSession(
            @AuthenticationPrincipal UserDetails userDetails) {

        SessionResponse session = sessionService.createSession(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }


    @PostMapping("/{id}/entries")
    @Operation(summary = "Add a symptom entry to an open session",
               description = "Append one symptom to the session. Call this multiple times to describe all symptoms before triggering triage.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Symptom entry recorded",
                     content = @Content(schema = @Schema(implementation = SymptomEntryResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error or session already closed",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Session not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SymptomEntryResponse> addEntry(
            @Parameter(description = "Session ID", required = true) @PathVariable UUID id,
            @Valid @RequestBody SymptomEntryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        SymptomEntryResponse entry = sessionService.addEntry(id, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
    }


    @PostMapping("/{id}/triage")
    @Operation(summary = "Send symptoms to AI and get triage classification",
               description = "Submits all symptom entries in the session to OpenAI for analysis. Returns a triage level (GREEN / AMBER / RED), probable conditions, escalation advice, and matched health tips. Closes the session permanently.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Triage completed",
                     content = @Content(schema = @Schema(implementation = TriageResultResponse.class))),
        @ApiResponse(responseCode = "400", description = "Session already triaged, already closed, or has no symptoms",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Session not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TriageResultResponse> performTriage(
            @Parameter(description = "Session ID", required = true) @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        TriageResultResponse result = triageService.performTriage(id, userDetails.getUsername());
        return ResponseEntity.ok(result);
    }


    @GetMapping("/{id}/result")
    @Operation(summary = "Get the triage result for a completed session",
               description = "Retrieves a previously generated triage result. Returns 404 if the session has not been triaged yet.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Triage result returned",
                     content = @Content(schema = @Schema(implementation = TriageResultResponse.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Session not found or not yet triaged",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TriageResultResponse> getResult(
            @Parameter(description = "Session ID", required = true) @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        TriageResultResponse result = triageService.getResult(id, userDetails.getUsername());
        return ResponseEntity.ok(result);
    }
}
