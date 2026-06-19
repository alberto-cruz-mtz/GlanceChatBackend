package alberto.cruz.mtz.glance.chat.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record TotpCodeWithTemporaryTokenRequest(
        @JsonProperty("code")
        @NotEmpty(message = "The code is required")
        @Size(min = 6, max = 6, message = "The code must be exactly 6 digits")
        String totpCode,

        @JsonProperty("token")
        @NotEmpty(message = "The token is required")
        String temporaryToken
) {
}