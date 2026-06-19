package alberto.cruz.mtz.glance.chat.backend.controller.rest;

import alberto.cruz.mtz.glance.chat.backend.dto.AccessTokenResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.AuthenticationRequest;
import alberto.cruz.mtz.glance.chat.backend.dto.AuthenticationResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.Secret2faResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.TotpCode;
import alberto.cruz.mtz.glance.chat.backend.dto.TotpCodeWithTemporaryTokenRequest;
import alberto.cruz.mtz.glance.chat.backend.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<AuthenticationResponse> registerNewUser(@RequestBody @Valid AuthenticationRequest request) {
        var response = this.authenticationService.register(request.username(), request.password());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticateUser(@RequestBody @Valid AuthenticationRequest request) {
        var response = this.authenticationService.authenticate(request.username(), request.password());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/2fa")
    public ResponseEntity<AccessTokenResponse> validateTotpCode(@RequestBody @Valid TotpCodeWithTemporaryTokenRequest request) {
        var response = this.authenticationService.verifyTotpCodeAndGenerateAccessToken(request.temporaryToken(), request.totpCode());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/2fa/generate")
    public ResponseEntity<Secret2faResponse> generateSecret2fa(Authentication authentication) {
        IO.println(authentication);
        String secret = this.authenticationService.generateSecretForActive2fa(authentication.getName());
        return ResponseEntity.ok(new Secret2faResponse(secret));
    }

    @PostMapping("/2fa/enable")
    public ResponseEntity<Void> active2fa(Authentication authentication, @RequestBody @Valid TotpCode totpCode) {
        this.authenticationService.validateTheFirstOtpCodeAndActive2fa(authentication.getName(), totpCode.value());
        return ResponseEntity.noContent().build();
    }

}
