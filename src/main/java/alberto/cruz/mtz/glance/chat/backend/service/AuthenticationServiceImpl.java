package alberto.cruz.mtz.glance.chat.backend.service;

import alberto.cruz.mtz.glance.chat.backend.dto.AuthenticationResponse;
import alberto.cruz.mtz.glance.chat.backend.exception.UserNotFoundException;
import alberto.cruz.mtz.glance.chat.backend.exception.UsernameAlreadyInUseException;
import alberto.cruz.mtz.glance.chat.backend.model.User;
import alberto.cruz.mtz.glance.chat.backend.repository.UserRepository;
import alberto.cruz.mtz.glance.chat.backend.util.JwtUtil;
import alberto.cruz.mtz.glance.chat.backend.util.PublicIdGenerator;
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