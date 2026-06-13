package alberto.cruz.mtz.glance.chat.backend.dto;

import jakarta.validation.constraints.NotEmpty;

public record ProfileRequest(
        @NotEmpty(message = "The ID is required") String id,
        String username
) {
}
