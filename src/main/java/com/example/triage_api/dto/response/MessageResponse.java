package com.example.triage_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.UUID;


@Data @Builder @AllArgsConstructor @NoArgsConstructor
@Schema(description = "A single chat message in a consultation")
public class MessageResponse {

    @Schema(description = "Unique message identifier")
    private UUID messageId;

    @Schema(description = "Conversation this message belongs to")
    private UUID conversationId;

    @Schema(description = "Who sent the message", example = "PATIENT", allowableValues = {"PATIENT", "DOCTOR"})
    private String senderType;

    @Schema(description = "UUID of the sender (patient userId or doctor doctorId)")
    private UUID senderId;

    @Schema(description = "Display name of the sender", example = "Dr. Amina Bello")
    private String senderName;

    @Schema(description = "Message text content", example = "The pain has been getting worse since this morning.")
    private String content;

    @Schema(description = "True if the other party has read this message")
    private Boolean isRead;

    @Schema(description = "When the message was sent (UTC)")
    private Instant sentAt;
}
