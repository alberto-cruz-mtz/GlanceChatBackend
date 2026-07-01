package alberto.cruz.mtz.glance.chat.backend.service;

import alberto.cruz.mtz.glance.chat.backend.dto.AccessTokenResponse;
import alberto.cruz.mtz.glance.chat.backend.exception.InvalidJwtException;
import alberto.cruz.mtz.glance.chat.backend.exception.InvalidTemporaryTokenException;
import alberto.cruz.mtz.glance.chat.backend.exception.InvalidTotpCodeException;
import alberto.cruz.mtz.glance.chat.backend.exception.TwoFactorAuthenticationNotActiveException;
import alberto.cruz.mtz.glance.chat.backend.exception.UserNotFoundException;
import alberto.cruz.mtz.glance.chat.backend.model.User;
import alberto.cruz.mtz.glance.chat.backend.repository.UserRepository;
import alberto.cruz.mtz.glance.chat.backend.util.JwtUtil;
import alberto.cruz.mtz.glance.chat.backend.util.TotpAuthenticator;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TwoFactorAuthenticationServiceImpl implements TwoFactorAuthenticationService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final TotpAuthenticator totpAuthenticator;

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
                .orElseThrow(() -> new UserNotFoundException("User not found with displayName: " + username));

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

    private String generateAccessToken(String username, String userId) {
        return jwtUtil.generateToken(username, userId, UUID.randomUUID().toString());
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found after successful authentication: " + username));
    }
}