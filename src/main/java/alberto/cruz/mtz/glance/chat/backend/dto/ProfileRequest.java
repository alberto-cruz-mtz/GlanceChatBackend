package alberto.cruz.mtz.glance.chat.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record ProfileRequest(
        @Size(min = 5, max = 50, message = "The displayName must be between 5 and 50 characters")
        @NotEmpty(message = "The displayName is required") String displayName
) {
}
