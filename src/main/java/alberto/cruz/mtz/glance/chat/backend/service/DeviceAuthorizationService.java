package alberto.cruz.mtz.glance.chat.backend.service;

import alberto.cruz.mtz.glance.chat.backend.dto.AuthenticationResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.DeviceCodeResponse;

import java.util.Optional;

public interface DeviceAuthorizationService {

    DeviceCodeResponse generateDeviceCode(String deviceName, String os);

    void authorizeDevice(String deviceCode, String username);

    Optional<AuthenticationResponse> checkDeviceCodeStatus(String deviceCode);
}
