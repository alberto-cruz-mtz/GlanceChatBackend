package alberto.cruz.mtz.glance.chat.backend.dto;

import alberto.cruz.mtz.glance.chat.backend.model.User;
import alberto.cruz.mtz.glance.chat.backend.util.PublicIdGenerator;

public record AuthenticationResponse(
        String id,
        String publicId,
        String accessToken,
        String avatar,
        String username,
        boolean hasSetUpProfile,
        boolean requiredAuthenticateFor2FA
) {

    public static AuthenticationResponse create(User user, String token) {
        String formattedPublicId = PublicIdGenerator.formatPublicIdForDisplay(user.getPublicId());
        return new AuthenticationResponse(
                user.getId(),
                formattedPublicId,
                token,
                user.getAvatarUrl(),
                user.getUsername(),
                user.isHasSetUpProfile(),
                user.isEnabled2fa()
        );
    }
}
