package com.example.triage_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.UUID;


@Data @Builder @AllArgsConstructor @NoArgsConstructor
@Schema(description = "Patient-doctor consultation summary")
public class ConversationResponse {

    @Schema(description = "Unique conversation identifier")
    private UUID conversationId;

    @Schema(description = "Patient's user ID")
    private UUID patientId;

    @Schema(description = "Patient's display name", example = "Jane Doe")
    private String patientName;

    @Schema(description = "Doctor's ID")
    private UUID doctorId;

    @Schema(description = "Doctor's display name", example = "Dr. Amina Bello")
    private String doctorName;

    @Schema(description = "Doctor's medical specialty", example = "General Practice")
    private String doctorSpecialty;

    @Schema(description = "Conversation lifecycle state", example = "ACTIVE",
            allowableValues = {"PENDING", "ACTIVE", "CLOSED", "DECLINED"})
    private String status;

    @Schema(description = "Patient's stated chief complaint", example = "Recurring chest tightness over the past week")
    private String chiefComplaint;

    @Schema(description = "UUID of a linked triage session, if provided at creation")
    private UUID linkedSessionId;

    @Schema(description = "Number of unread messages from the other party", example = "2")
    private long unreadCount;

    @Schema(description = "When the consultation was requested (UTC)")
    private Instant createdAt;

    @Schema(description = "When the conversation was last updated (UTC)")
    private Instant updatedAt;

    @Schema(description = "When the doctor accepted the consultation (UTC); null if not yet accepted")
    private Instant acceptedAt;

    @Schema(description = "When the conversation was closed (UTC); null if still open")
    private Instant closedAt;
}
