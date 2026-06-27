package alberto.cruz.mtz.glance.chat.backend.dto;

import java.time.Instant;

public record DeviceCodeResponse(
        String deviceCode,
        int expiresIn
) {
}
