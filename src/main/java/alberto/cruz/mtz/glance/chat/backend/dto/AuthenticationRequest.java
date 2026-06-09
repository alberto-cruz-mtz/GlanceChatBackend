package alberto.cruz.mtz.glance.chat.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record AuthenticationRequest(
        @Email(message = "The email must be a valid email address")
        @NotEmpty(message = "The email must not be empty")
        String email
) {
}
