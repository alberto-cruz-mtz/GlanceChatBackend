package alberto.cruz.mtz.glance.chat.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record AuthenticationWithOtpRequest(
        @Email(message = "The email must be a valid email address")
        @NotEmpty(message = "The email must not be empty")
        String email,
        @NotEmpty(message = "The OTP must not be empty")
        String OTP
) {
}
