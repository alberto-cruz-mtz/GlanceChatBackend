package alberto.cruz.mtz.glance.chat.backend.service;

import alberto.cruz.mtz.glance.chat.backend.dto.AuthenticationResponse;

public interface AuthenticationService {

    void sendAuthenticationOtpCode(String email);

    AuthenticationResponse loginWithOtpCode(String email, String otpCode);

    void sendRegistrationOtpCode(String email);

    AuthenticationResponse registerWithOtpCode(String email, String otpCode);
}
