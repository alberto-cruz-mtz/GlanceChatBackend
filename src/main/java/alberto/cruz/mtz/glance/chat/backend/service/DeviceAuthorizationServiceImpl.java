package alberto.cruz.mtz.glance.chat.backend.service;

import alberto.cruz.mtz.glance.chat.backend.dto.AuthenticationResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.Device;
import alberto.cruz.mtz.glance.chat.backend.dto.DeviceCodeResponse;
import alberto.cruz.mtz.glance.chat.backend.exception.DeviceCodeHasAlreadyBeenUsedException;
import alberto.cruz.mtz.glance.chat.backend.exception.InvalidOrExpiredDeviceCodeException;
import alberto.cruz.mtz.glance.chat.backend.exception.UserNotFoundException;
import alberto.cruz.mtz.glance.chat.backend.model.Session;
import alberto.cruz.mtz.glance.chat.backend.model.User;
import alberto.cruz.mtz.glance.chat.backend.repository.SessionRepository;
import alberto.cruz.mtz.glance.chat.backend.repository.UserRepository;
import alberto.cruz.mtz.glance.chat.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceAuthorizationServiceImpl implements DeviceAuthorizationService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final JwtUtil jwtUtil;

    private final Map<String, Device> sessionStorage = new HashMap<>();

    @Override
    public DeviceCodeResponse generateDeviceCode(String deviceName, String os) {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(1000000);
        String deviceCode = String.format("%06d", code);

        Device device = Device.create(deviceName, os, 300);
        this.sessionStorage.put(deviceCode, device);

        return new DeviceCodeResponse(deviceCode, 300);
    }

    @Override
    public void authorizeDevice(String deviceCode, String username) {
        Device device = this.sessionStorage.get(deviceCode);

        if (device == null || Instant.now().isAfter(device.expiration())) {
            throw new InvalidOrExpiredDeviceCodeException("The device code is invalid or has expired. Please request a new device code.");
        }

        if (!device.isPending()) {
            throw new DeviceCodeHasAlreadyBeenUsedException("Device code has already been used or is not pending authorization");
        }

        User user = this.findUserByUsername(username);
        Session session = new Session(null, device.name(), device.os(), UUID.randomUUID().toString(), true, user.getId(), Instant.now());
        sessionRepository.save(session);
        device.authorize();
        device.setUsername(username);
    }

    @Override
    public Optional<AuthenticationResponse> checkDeviceCodeStatus(String deviceCode) {
        Device device = this.sessionStorage.get(deviceCode);

        if (device == null || Instant.now().isAfter(device.expiration())) {
            throw new InvalidOrExpiredDeviceCodeException("The device code is invalid or has expired. Please request a new device code.");
        }

        if (device.isPending()) return Optional.empty();

        User user = this.findUserByUsername(device.getUsername());
        String token = this.generateAuthenticationToken(user);

        AuthenticationResponse response = AuthenticationResponse.create(user, token);

        return Optional.of(response);
    }

    private String generateAuthenticationToken(User user) {
        if (user.isEnabled2fa()) return jwtUtil.generateTemporaryToken(user.getUsername(), user.getId());

        return this.generateAccessToken(user.getUsername(), user.getId());
    }

    private String generateAccessToken(String username, String userId) {
        return jwtUtil.generateToken(username, userId, UUID.randomUUID().toString());
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found after successful authentication: " + username));
    }
}