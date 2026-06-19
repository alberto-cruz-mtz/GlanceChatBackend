package alberto.cruz.mtz.glance.chat.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AuthenticationRequest(
        @NotEmpty(message = "The username is required")
        String username,
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$", message = "The password must be at least 8 characters long and contain at least one letter and one number")
        @NotEmpty(message = "The password is required")
        @Size(min = 8, message = "The password must be at least 8 characters long")
        String password
) {
}
