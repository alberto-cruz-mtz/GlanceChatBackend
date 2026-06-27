package alberto.cruz.mtz.glance.chat.backend.service;

import alberto.cruz.mtz.glance.chat.backend.dto.AccessTokenResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.AuthenticationResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.Device;
import alberto.cruz.mtz.glance.chat.backend.dto.DeviceCodeResponse;
import alberto.cruz.mtz.glance.chat.backend.exception.DeviceCodeHasAlreadyBeenUsedException;
import alberto.cruz.mtz.glance.chat.backend.exception.InvalidJwtException;
import alberto.cruz.mtz.glance.chat.backend.exception.InvalidOrExpiredDeviceCodeException;
import alberto.cruz.mtz.glance.chat.backend.exception.InvalidTotpCodeException;
import alberto.cruz.mtz.glance.chat.backend.exception.InvalidTemporaryTokenException;
import alberto.cruz.mtz.glance.chat.backend.exception.TwoFactorAuthenticationNotActiveException;
import alberto.cruz.mtz.glance.chat.backend.exception.UserNotFoundException;
import alberto.cruz.mtz.glance.chat.backend.exception.UsernameAlreadyInUseException;
import alberto.cruz.mtz.glance.chat.backend.model.Session;
import alberto.cruz.mtz.glance.chat.backend.model.User;
import alberto.cruz.mtz.glance.chat.backend.repository.SessionRepository;
import alberto.cruz.mtz.glance.chat.backend.repository.UserRepository;
import alberto.cruz.mtz.glance.chat.backend.util.JwtUtil;
import alberto.cruz.mtz.glance.chat.backend.util.PublicIdGenerator;
import alberto.cruz.mtz.glance.chat.backend.util.TotpAuthenticator;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TotpAuthenticator totpAuthenticator;

    private final Map<String, Device> sessionStorage = new HashMap<>();


    @Override
    public AuthenticationResponse register(String username, String password) {
        this.validateIfUsernameAlreadyInUse(username);

        String publicId = this.generatePublicId();
        String passwordHash = passwordEncoder.encode(password);

        User user = User.create(username, passwordHash, publicId);
        User createdUser = userRepository.save(user);

        String token = this.generateAuthenticationToken(createdUser);

        return AuthenticationResponse.create(user, token);
    }

    @Override
    public AuthenticationResponse authenticate(String username, String password) {
        this.validateCredentials(username, password);

        User user = this.findUserByUsername(username);
        String token = this.generateAuthenticationToken(user);

        return AuthenticationResponse.create(user, token);
    }

    @Override
    public String generateSecretForActive2fa(String username) {
        String secret = totpAuthenticator.generateSecret();
        User user = this.findUserByUsername(username);
        user.setSecret(secret);
        userRepository.save(user);

        return secret;
    }

    @Override
    public void validateTheFirstOtpCodeAndActive2fa(String username, String code) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

        boolean isValid = totpAuthenticator.verifyCode(user.getSecret(), code);

        if (!isValid) throw new InvalidTotpCodeException("Invalid TOTP code provided for user: " + username);

        user.setEnabled2fa(true);
        userRepository.save(user);
    }

    @Override
    public AccessTokenResponse verifyTotpCodeAndGenerateAccessToken(String temporaryToken, String code) {
        String username;
        try {
            DecodedJWT decodedJWT = jwtUtil.verifyTemporaryToken(temporaryToken);
            username = jwtUtil.getUsername(decodedJWT);
        } catch (InvalidJwtException ignore) {
            throw new InvalidTemporaryTokenException("Invalid temporary token provided");
        }

        User user = this.findUserByUsername(username);

        if (user.getSecret() == null) {
            throw new TwoFactorAuthenticationNotActiveException("Two-factor authentication is not active for user: " + username);
        }

        boolean isValid = totpAuthenticator.verifyCode(user.getSecret(), code);

        if (!isValid) throw new InvalidTotpCodeException("Invalid TOTP code provided for user: " + username);

        String token = this.generateAccessToken(user.getUsername(), user.getId());
        return new AccessTokenResponse(token);
    }

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

    private void validateIfUsernameAlreadyInUse(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyInUseException("Username is already in use: " + username);
        }
    }

    private String generatePublicId() {
        while (true) {
            String publicId = PublicIdGenerator.generatePublicId();
            if (!userRepository.existsByPublicId(publicId)) return publicId;
        }
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found after successful authentication: " + username));
    }

    private void validateCredentials(String username, String password) {
        UsernamePasswordAuthenticationToken credentials = new UsernamePasswordAuthenticationToken(username, password, Collections.emptyList());
        // Lanza un AuthenticationException si las credenciales no son válidas
        authenticationManager.authenticate(credentials);
    }
}
