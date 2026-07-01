package com.example.triage_api.controller;

import com.example.triage_api.dto.request.SendMessageRequest;
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
@RequestMapping("/api/doctor-conversations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Doctor — Conversations", description = "Doctor-side consultation management: view requests, accept/decline, chat, and close")
public class DoctorConversationController {

    private final ConversationService conversationService;


    @GetMapping
    @Operation(summary = "List all my consultations",
               description = "Returns the authenticated doctor's consultations, newest first.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Conversation list returned"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<ConversationResponse>> getDoctorConversations(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(
                conversationService.getDoctorConversations(userDetails.getUsername(), pageable));
    }


    @GetMapping("/pending")
    @Operation(summary = "List pending consultation requests",
               description = "Returns consultations awaiting the doctor's accept/decline, oldest first.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pending request list returned"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<ConversationResponse>> getPendingRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(
                conversationService.getPendingRequests(userDetails.getUsername(), pageable));
    }


    @PutMapping("/{id}/accept")
    @Operation(summary = "Accept a consultation request",
               description = "Moves the conversation from PENDING to ACTIVE. The doctor's status is refreshed — they may become BUSY if at maximum active chats.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Consultation accepted",
                     content = @Content(schema = @Schema(implementation = ConversationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Conversation is not in PENDING state",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Conversation not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ConversationResponse> acceptConversation(
            @Parameter(description = "Conversation ID", required = true) @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                conversationService.acceptConversation(id, userDetails.getUsername()));
    }


    @PutMapping("/{id}/decline")
    @Operation(summary = "Decline a consultation request",
               description = "Moves the conversation from PENDING to DECLINED.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Consultation declined",
                     content = @Content(schema = @Schema(implementation = ConversationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Conversation is not in PENDING state",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Conversation not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ConversationResponse> declineConversation(
            @Parameter(description = "Conversation ID", required = true) @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                conversationService.declineConversation(id, userDetails.getUsername()));
    }


    @GetMapping("/{id}/messages")
    @Operation(summary = "Read messages in a conversation",
               description = "Returns all messages ordered oldest-first. Also marks unread patient messages as read.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Messages returned"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Conversation not found or not owned by this doctor",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<MessageResponse>> getDoctorMessages(
            @Parameter(description = "Conversation ID", required = true) @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                conversationService.getMessagesAsDoctor(id, userDetails.getUsername()));
    }


    @PostMapping("/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Send a message to the patient",
               description = "The conversation must be ACTIVE. Returns 400 if still PENDING or CLOSED.")
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
    public ResponseEntity<MessageResponse> sendDoctorMessage(
            @Parameter(description = "Conversation ID", required = true) @PathVariable UUID id,
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        MessageResponse message =
                conversationService.sendMessageAsDoctor(id, userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }


    @PutMapping("/{id}/close")
    @Operation(summary = "Close a conversation",
               description = "Marks the conversation as CLOSED.")
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
    public ResponseEntity<ConversationResponse> closeDoctorConversation(
            @Parameter(description = "Conversation ID", required = true) @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                conversationService.closeConversationAsDoctor(id, userDetails.getUsername()));
    }
}
