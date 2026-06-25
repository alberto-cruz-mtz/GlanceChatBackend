package alberto.cruz.mtz.glance.chat.backend.service;

import alberto.cruz.mtz.glance.chat.backend.dto.AccessTokenResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.AuthenticationResponse;
import alberto.cruz.mtz.glance.chat.backend.exception.InvalidJwtException;
import alberto.cruz.mtz.glance.chat.backend.exception.UnknownException;
import alberto.cruz.mtz.glance.chat.backend.exception.UserNotFoundException;
import alberto.cruz.mtz.glance.chat.backend.exception.UsernameAlreadyInUseException;
import alberto.cruz.mtz.glance.chat.backend.model.User;
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

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TotpAuthenticator totpAuthenticator;


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

        if (!isValid) throw new UnknownException("Invalid OTP code, try again");

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
            throw new UnknownException("Token invalid, please authenticate again");
        }

        User user = this.findUserByUsername(username);

        if (user.getSecret() == null) {
            throw new UnknownException("2FA is not active for this user");
        }

        boolean isValid = totpAuthenticator.verifyCode(user.getSecret(), code);

        if (!isValid) throw new UnknownException("TOTP code is invalid");

        String token = this.generateAccessToken(user.getUsername(), user.getId());
        return new AccessTokenResponse(token);
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
