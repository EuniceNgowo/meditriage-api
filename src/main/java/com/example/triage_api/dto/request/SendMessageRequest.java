package com.example.triage_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
@Schema(description = "Chat message payload")
public class SendMessageRequest {

    @NotBlank(message = "Message content cannot be empty")
    @Schema(description = "Message text", example = "The pain has been getting worse since this morning.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
}
