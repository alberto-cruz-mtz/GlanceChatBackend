package alberto.cruz.mtz.glance.chat.backend.service;

import alberto.cruz.mtz.glance.chat.backend.dto.AccessTokenResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.AuthenticationResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.DeviceCodeResponse;

import java.util.Optional;

public interface AuthenticationService {

    AuthenticationResponse register(String username, String password);

    AuthenticationResponse authenticate(String username, String password);

}
