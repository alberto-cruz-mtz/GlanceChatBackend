package alberto.cruz.mtz.glance.chat.backend.service;

import alberto.cruz.mtz.glance.chat.backend.dto.AuthenticationResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.OtpCode;
import alberto.cruz.mtz.glance.chat.backend.exception.UserNotFoundException;
import alberto.cruz.mtz.glance.chat.backend.model.User;
import alberto.cruz.mtz.glance.chat.backend.repository.UserRepository;
import alberto.cruz.mtz.glance.chat.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private final Map<String, OtpCode> OTPStorage = new HashMap<>();
    private static final int FIVE_MINUTES_IN_SECONDS = 300;

    @Override
    public AuthenticationResponse authenticate(String email, String OTP) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));

        var otpCode = this.OTPStorage.get(email);
        if (otpCode == null || !otpCode.value().equals(OTP) || otpCode.expiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }

        String token = jwtUtil.generateToken(user.getDisplayName(), user.getId(), UUID.randomUUID().toString());

        return new AuthenticationResponse(token, Instant.now(), user.getId());
    }

    @Override
    public void prepareBeforeAuthenticate(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));

        SecureRandom random = new SecureRandom();
        int otp = random.nextInt(1000000);
        String otpString = String.format("%06d", otp);

        var otpCode = new OtpCode(otpString, user.getEmail(), Instant.now().plusSeconds(FIVE_MINUTES_IN_SECONDS));
        this.OTPStorage.put(email, otpCode);

        sender(email, otpString);
    }

    @Override
    public void prepareBeforeRegister(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserNotFoundException("User with email " + email + " already exists");
        }

        SecureRandom random = new SecureRandom();
        int otp = random.nextInt(1000000);
        String otpString = String.format("%06d", otp);

        var otpCode = new OtpCode(otpString, email, Instant.now().plusSeconds(FIVE_MINUTES_IN_SECONDS));
        this.OTPStorage.put(email, otpCode);

        sender(email, otpString);
    }

    @Override
    public AuthenticationResponse register(String email, String OTP) {
        if (userRepository.existsByEmail(email)) {
            throw new UserNotFoundException("User with email " + email + " already exists");
        }

        var otpCode = this.OTPStorage.get(email);
        if (otpCode == null || !otpCode.value().equals(OTP) || otpCode.expiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }

        User user = User.create(email);
        User createdUser = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getDisplayName(), user.getId(), UUID.randomUUID().toString());

        return new AuthenticationResponse(token, Instant.now(), createdUser.getId());
    }


    private void sender(String emailAddress, String OTP) {
        IO.println("Sending email to: " + emailAddress + " with code: " + OTP);
    }
}
