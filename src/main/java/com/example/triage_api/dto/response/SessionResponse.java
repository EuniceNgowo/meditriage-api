package com.example.triage_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.UUID;


@Data @Builder @AllArgsConstructor @NoArgsConstructor
@Schema(description = "Symptom session summary")
public class SessionResponse {

    @Schema(description = "Unique session identifier")
    private UUID sessionId;

    @Schema(description = "Session lifecycle state", example = "OPEN", allowableValues = {"OPEN", "CLOSED"})
    private String status;

    @Schema(description = "When the session was opened (UTC)")
    private Instant startedAt;

    @Schema(description = "When the session was closed (UTC); null if still open")
    private Instant endedAt;

    @Schema(description = "Number of symptom entries added to this session", example = "3")
    private int entryCount;

    @Schema(description = "True if a triage result has been generated for this session")
    private boolean hasTriageResult;
}
