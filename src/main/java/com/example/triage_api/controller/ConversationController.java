package com.example.triage_api.controller;

import com.example.triage_api.dto.request.RateConversationRequest;
import com.example.triage_api.dto.request.SendMessageRequest;
import com.example.triage_api.dto.request.StartConversationRequest;
import com.example.triage_api.dto.response.ConversationResponse;
import com.example.triage_api.dto.response.MessageResponse;
import com.example.triage_api.exception.ErrorResponse;
import com.example.triage_api.service.ConversationService;
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
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Patient — Conversations", description = "Patient-side consultation requests, messaging, and rating")
public class ConversationController {

    private final ConversationService conversationService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Request a consultation with a doctor",
               description = "Creates a new consultation in PENDING state. The doctor must accept it before messages can be exchanged. Optionally link a completed triage session to give the doctor context.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Consultation request created",
                     content = @Content(schema = @Schema(implementation = ConversationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Doctor is OFFLINE/BUSY, or an open consultation with this doctor already exists",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Doctor not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ConversationResponse> startConversation(
            @Valid @RequestBody StartConversationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        ConversationResponse response =
                conversationService.startConversation(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping
    @Operation(summary = "List all my consultations",
               description = "Returns the authenticated patient's consultations, newest first.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Consultation list returned"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<ConversationResponse>> getMyConversations(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(
                conversationService.getPatientConversations(userDetails.getUsername(), pageable));
    }


    @GetMapping("/{id}/messages")
    @Operation(summary = "Read messages in a conversation",
               description = "Returns all messages ordered oldest-first. Also marks unread doctor messages as read.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Messages returned"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Conversation not found or not owned by this patient",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<MessageResponse>> getMessages(
            @Parameter(description = "Conversation ID", required = true) @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                conversationService.getMessagesAsPatient(id, userDetails.getUsername()));
    }


    @PostMapping("/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Send a message to the doctor",
               description = "The conversation must be ACTIVE (doctor has accepted). Returns 400 if still PENDING or CLOSED.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Message sent",
                     content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Conversation is not ACTIVE",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Conversation not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MessageResponse> sendMessage(
            @Parameter(description = "Conversation ID", required = true) @PathVariable UUID id,
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        MessageResponse message =
                conversationService.sendMessageAsPatient(id, userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }


    @PutMapping("/{id}/close")
    @Operation(summary = "Close a consultation",
               description = "Marks the conversation as CLOSED. After closing, the patient can rate the doctor.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Conversation closed",
                     content = @Content(schema = @Schema(implementation = ConversationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Conversation is already closed",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Conversation not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ConversationResponse> closeConversation(
            @Parameter(description = "Conversation ID", required = true) @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                conversationService.closeConversationAsPatient(id, userDetails.getUsername()));
    }


    @PostMapping("/{id}/rate")
    @Operation(summary = "Rate the doctor after a closed consultation",
               description = "Submit a 1-5 star rating and optional feedback. Can only be done once per conversation, and only after it is CLOSED.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rating submitted",
                     content = @Content(schema = @Schema(implementation = ConversationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Conversation not closed, or already rated",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Conversation not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ConversationResponse> rateConversation(
            @Parameter(description = "Conversation ID", required = true) @PathVariable UUID id,
            @Valid @RequestBody RateConversationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                conversationService.rateConversation(id, userDetails.getUsername(), request));
    }
}
