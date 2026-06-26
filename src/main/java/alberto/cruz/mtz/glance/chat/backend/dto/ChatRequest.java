package alberto.cruz.mtz.glance.chat.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record ChatRequest(
        @Pattern(regexp = "^[A-Z0-9]{8}", message = "Recipient public ID must be 8 characters long and contain only uppercase letters and numbers")
        @NotEmpty(message = "Recipient public ID cannot be empty")
        String recipientPublicId
) {
}
