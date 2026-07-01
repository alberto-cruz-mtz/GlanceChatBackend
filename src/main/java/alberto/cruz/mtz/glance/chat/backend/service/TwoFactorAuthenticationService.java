package alberto.cruz.mtz.glance.chat.backend.service;

import alberto.cruz.mtz.glance.chat.backend.dto.AccessTokenResponse;

public interface TwoFactorAuthenticationService {

    String generateSecretForActive2fa(String username);

    void validateTheFirstOtpCodeAndActive2fa(String username, String code);

    AccessTokenResponse verifyTotpCodeAndGenerateAccessToken(String token, String code);
}
