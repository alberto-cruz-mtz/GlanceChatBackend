package alberto.cruz.mtz.glance.chat.backend.dto;

import alberto.cruz.mtz.glance.chat.backend.model.User;

import java.time.Instant;

public record AuthenticationResponse(
        String accessToken,
        String id,
        String publicId,
        String avatar,
        String username,
        boolean hasSetUpProfile
) {

    public static AuthenticationResponse create(String token, String publicId, User user) {
        return new AuthenticationResponse(token, user.getId(), publicId, user.getAvatarUrl(), user.getDisplayName(), user.isHasSetUpProfile());
    }
}
