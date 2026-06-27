package alberto.cruz.mtz.glance.chat.backend.dto;

import jakarta.validation.constraints.NotEmpty;

public record DeviceCodeRequest(
        @NotEmpty(message = "The device name is required")
        String deviceName,
        @NotEmpty(message = "The OS version is required")
        String osVersion
) {
}
