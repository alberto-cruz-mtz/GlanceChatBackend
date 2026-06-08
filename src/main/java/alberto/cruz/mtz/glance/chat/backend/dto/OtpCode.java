package alberto.cruz.mtz.glance.chat.backend.dto;

import java.time.Instant;

public record OtpCode(String value, String otpCodeAssociatedWithEmailAddress, Instant expiresAt) {
}
