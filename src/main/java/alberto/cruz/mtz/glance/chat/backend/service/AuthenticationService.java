package alberto.cruz.mtz.glance.chat.backend.service;

import alberto.cruz.mtz.glance.chat.backend.dto.AuthenticationResponse;

public interface AuthenticationService {

    void prepareBeforeAuthenticate(String email);

    AuthenticationResponse authenticate(String email, String OTP);

    void prepareBeforeRegister(String email);

    AuthenticationResponse register(String email, String OTP);
}
