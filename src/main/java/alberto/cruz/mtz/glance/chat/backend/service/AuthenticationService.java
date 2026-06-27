package alberto.cruz.mtz.glance.chat.backend.service;

import alberto.cruz.mtz.glance.chat.backend.dto.AccessTokenResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.AuthenticationResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.DeviceCodeResponse;

import java.util.Optional;

public interface AuthenticationService {

    AuthenticationResponse register(String username, String password);

    AuthenticationResponse authenticate(String username, String password);

    String generateSecretForActive2fa(String username);

    void validateTheFirstOtpCodeAndActive2fa(String username, String code);

    AccessTokenResponse verifyTotpCodeAndGenerateAccessToken(String token, String code);

    DeviceCodeResponse generateDeviceCode(String deviceName, String os);

    void authorizeDevice(String deviceCode, String username);

    Optional<AuthenticationResponse> checkDeviceCodeStatus(String deviceCode);
}
