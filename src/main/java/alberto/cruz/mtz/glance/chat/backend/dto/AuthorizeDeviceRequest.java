package alberto.cruz.mtz.glance.chat.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record AuthorizeDeviceRequest(
        @Pattern(regexp = "^[0-9]{6}$", message = "Invalid device code format. It must be a 6-digit number.")
        @NotEmpty(message = "Device code must not be empty")
        String deviceCode
) {
}
