package alberto.cruz.mtz.glance.chat.backend.dto;

import java.time.Instant;

public record AuthenticationResponse(
        String accessToken,
        Instant timestamp,
        String id
) {
}
