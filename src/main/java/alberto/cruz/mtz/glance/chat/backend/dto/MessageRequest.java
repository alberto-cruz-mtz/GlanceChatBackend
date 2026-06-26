package alberto.cruz.mtz.glance.chat.backend.dto;

import jakarta.validation.constraints.NotEmpty;

public record MessageRequest(
        @NotEmpty(message = "The sender id is required") String senderId,
        @NotEmpty(message = "The message content is not empty") String content,
        @NotEmpty(message = "The recipient id is required") String recipientId
) {
}
