package alberto.cruz.mtz.glance.chat.backend.dto;

import java.time.Instant;

public record OtpCode(String value, String otpCodeAssociatedWithEmailAddress, Instant expiresAt) {

    public boolean isExpired() {
        return this.expiresAt.isBefore(Instant.now());
    }

    public boolean otpCodeMatches(String otpCode) {
        return this.value.equals(otpCode);
    }
}
