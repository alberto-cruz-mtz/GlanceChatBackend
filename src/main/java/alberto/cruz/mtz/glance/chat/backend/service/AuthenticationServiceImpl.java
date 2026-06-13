package alberto.cruz.mtz.glance.chat.backend.service;

import alberto.cruz.mtz.glance.chat.backend.dto.AuthenticationResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.OtpCode;
import alberto.cruz.mtz.glance.chat.backend.exception.EmailAlreadyInUseException;
import alberto.cruz.mtz.glance.chat.backend.exception.InvalidOtpException;
import alberto.cruz.mtz.glance.chat.backend.exception.UserNotFoundException;
import alberto.cruz.mtz.glance.chat.backend.model.User;
import alberto.cruz.mtz.glance.chat.backend.repository.UserRepository;
import alberto.cruz.mtz.glance.chat.backend.util.EmailSender;
import alberto.cruz.mtz.glance.chat.backend.util.JwtUtil;
import alberto.cruz.mtz.glance.chat.backend.util.PublicIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final EmailSender emailSender;
    private final JwtUtil jwtUtil;

    private final Map<String, OtpCode> otpCodeCache = new HashMap<>();
    private static final int OTP_CODE_EXPIRATION_IN_SECONDS = 300;

    @Override
    public AuthenticationResponse loginWithOtpCode(String email, String otpCode) {
        var user = this.findUserByEmail(email);
        this.validateOtpCode(email, otpCode);

        String accessToken = jwtUtil.generateToken(user.getDisplayName(), user.getId(), UUID.randomUUID().toString());
        String publicId = PublicIdGenerator.formatPublicIdForDisplay(user.getPublicId());

        return AuthenticationResponse.create(accessToken, publicId, user);
    }

    @Override
    public void sendAuthenticationOtpCode(String email) {
        this.verifyUserExists(email);
        var otpCode = this.generateOtpCode(email);

        this.otpCodeCache.put(email, otpCode);
        emailSender.sendEmailWithOtpCode(email, otpCode.value());
    }

    @Override
    public void sendRegistrationOtpCode(String email) {
        this.verifyEmailNotRegistered(email);
        var otpCode = this.generateOtpCode(email);
        this.otpCodeCache.put(email, otpCode);

        emailSender.sendEmailWithOtpCode(email, otpCode.value());
    }

    @Override
    public AuthenticationResponse registerWithOtpCode(String email, String otpCode) {
        this.verifyEmailNotRegistered(email);
        this.validateOtpCode(email, otpCode);

        String publicId = this.generateUniquePublicId();

        User user = User.create(email, publicId);
        User createdUser = userRepository.save(user);

        String accessToken = jwtUtil.generateToken(user.getDisplayName(), user.getId(), UUID.randomUUID().toString());
        String publicIdFormatted = PublicIdGenerator.formatPublicIdForDisplay(createdUser.getPublicId());
        return AuthenticationResponse.create(accessToken, publicIdFormatted, createdUser);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email address [" + email + "] was not found"));
    }

    private OtpCode generateOtpCode(String email) {
        SecureRandom random = new SecureRandom();
        int otp = random.nextInt(1000000);
        String formattedOtpCode = String.format("%06d", otp);

        return new OtpCode(formattedOtpCode, email, Instant.now().plusSeconds(OTP_CODE_EXPIRATION_IN_SECONDS));
    }

    private void validateOtpCode(String email, String otpCode) {
        var cachedOtpCode = this.otpCodeCache.get(email);

        if (cachedOtpCode == null || !cachedOtpCode.otpCodeMatches(otpCode) || cachedOtpCode.isExpired()) {
            throw new InvalidOtpException("The OTP code is invalid or has expired. Please request a new code.");
        }
    }

    private void verifyEmailNotRegistered(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyInUseException("A user with email address [" + email + "] is already registered");
        }
    }

    private void verifyUserExists(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new UserNotFoundException("User with email address [" + email + "] does not exist");
        }
    }

    private String generateUniquePublicId() {
        while (true) {
            String publicId = PublicIdGenerator.generatePublicId();
            if (!userRepository.existsByPublicId(publicId)) {
                return publicId;
            }
        }
    }
}
