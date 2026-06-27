package alberto.cruz.mtz.glance.chat.backend.controller.rest;

import alberto.cruz.mtz.glance.chat.backend.dto.AccessTokenResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.AuthenticationRequest;
import alberto.cruz.mtz.glance.chat.backend.dto.AuthenticationResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.AuthorizeDeviceRequest;
import alberto.cruz.mtz.glance.chat.backend.dto.DeviceCodeRequest;
import alberto.cruz.mtz.glance.chat.backend.dto.DeviceCodeResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.Secret2faResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.TotpCode;
import alberto.cruz.mtz.glance.chat.backend.dto.TotpCodeWithTemporaryTokenRequest;
import alberto.cruz.mtz.glance.chat.backend.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration, login and two-factor authentication")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Register a new user", description = "Creates a new user account with username and password. Returns an access token upon successful registration.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Username already in use",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class, example = "{\"type\":\"https://example.com/username-already-in-use\",\"title\":\"Username already in use\",\"status\":409,\"detail\":\"The username is already taken. Please choose a different one.\"}")))
    })
    @PostMapping("/signup")
    public ResponseEntity<AuthenticationResponse> registerNewUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User credentials for registration",
                    required = true)
            @RequestBody @Valid AuthenticationRequest request) {
        var response = this.authenticationService.register(request.username(), request.password());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Authenticate a user", description = "Authenticates a user with username and password. If two-factor authentication is enabled, a temporary token is returned instead of an access token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User authenticated successfully",
                    content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticateUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User credentials for authentication",
                    required = true)
            @RequestBody @Valid AuthenticationRequest request) {
        var response = this.authenticationService.authenticate(request.username(), request.password());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Validate TOTP code and generate access token", description = "Validates a TOTP code along with a temporary token (issued during login when 2FA is active) and returns a permanent access token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "TOTP code validated, access token issued",
                    content = @Content(schema = @Schema(implementation = AccessTokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed or invalid TOTP code",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Invalid temporary token or two-factor authentication not active",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/login/2fa")
    public ResponseEntity<AccessTokenResponse> validateTotpCode(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Temporary token and TOTP code for 2FA validation",
                    required = true)
            @RequestBody @Valid TotpCodeWithTemporaryTokenRequest request) {
        var response = this.authenticationService.verifyTotpCodeAndGenerateAccessToken(request.temporaryToken(), request.totpCode());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Generate 2FA secret", description = "Generates a new TOTP secret for the authenticated user. This secret is used to set up two-factor authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Secret generated successfully",
                    content = @Content(schema = @Schema(implementation = Secret2faResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/2fa/generate")
    public ResponseEntity<Secret2faResponse> generateSecret2fa(
            @Parameter(description = "Authenticated user", hidden = true)
            Authentication authentication) {
        IO.println(authentication);
        String secret = this.authenticationService.generateSecretForActive2fa(authentication.getName());
        return ResponseEntity.ok(new Secret2faResponse(secret));
    }

    @Operation(summary = "Enable two-factor authentication", description = "Validates the first TOTP code and activates two-factor authentication for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Two-factor authentication enabled successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed or invalid TOTP code",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/2fa/enable")
    public ResponseEntity<Void> active2fa(
            @Parameter(description = "Authenticated user", hidden = true)
            Authentication authentication,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "TOTP code to validate for 2FA activation",
                    required = true)
            @RequestBody @Valid TotpCode totpCode) {
        this.authenticationService.validateTheFirstOtpCodeAndActive2fa(authentication.getName(), totpCode.value());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/devices/request-code")
    public ResponseEntity<DeviceCodeResponse> getDeviceCode(@RequestBody @Valid DeviceCodeRequest request) {
        var response = this.authenticationService.generateDeviceCode(request.deviceName(), request.osVersion());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/devices/authorize")
    public ResponseEntity<Void> authorizeDevice(@RequestBody @Valid AuthorizeDeviceRequest request, Authentication authentication) {
        String username = authentication.getName();
        this.authenticationService.authorizeDevice(request.deviceCode(), username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/devices/checked")
    public ResponseEntity<?> authenticateUserWithDeviceCode(@RequestBody @Valid AuthorizeDeviceRequest request) {
        var response = this.authenticationService.checkDeviceCodeStatus(request.deviceCode());

        if (response.isEmpty()) return ResponseEntity.accepted().build();

        return ResponseEntity.ok(response.get());
    }
}
