package com.example.triage_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;


@Data
@Schema(description = "Request to start a consultation with a doctor")
public class StartConversationRequest {

    @NotNull(message = "Doctor ID is required")
    @Schema(description = "UUID of the doctor to consult", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID doctorId;

    @Schema(description = "Brief summary of the main concern to share with the doctor", example = "Recurring chest tightness over the past week")
    private String chiefComplaint;

    @Schema(description = "Optional: UUID of a completed triage session to share context with the doctor")
    private UUID linkedSessionId;
}
