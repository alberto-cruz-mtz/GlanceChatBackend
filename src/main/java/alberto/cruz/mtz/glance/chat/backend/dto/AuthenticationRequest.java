package alberto.cruz.mtz.glance.chat.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AuthenticationRequest(
        @Pattern(regexp = "^[a-zA-Z0-9_]{3,25}$", message = "The displayName can only contain letters, numbers and underscores")
        @Size(min = 5, max = 25, message = "The displayName must be between 5 and 25 characters long")
        @NotEmpty(message = "The displayName is required")
        String username,
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$", message = "The password must be at least 8 characters long and contain at least one letter and one number")
        @NotEmpty(message = "The password is required")
        @Size(min = 8, message = "The password must be at least 8 characters long")
        String password
) {
}
