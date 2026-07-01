package alberto.cruz.mtz.glance.chat.backend.controller.rest;

import alberto.cruz.mtz.glance.chat.backend.dto.AccessTokenResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.Secret2faResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.TotpCode;
import alberto.cruz.mtz.glance.chat.backend.dto.TotpCodeWithTemporaryTokenRequest;
import alberto.cruz.mtz.glance.chat.backend.service.TwoFactorAuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/2fa")
@RequiredArgsConstructor
public class TwoFactorAuthenticationController {

    private final TwoFactorAuthenticationService twoFactorAuthenticationService;

    @Operation(summary = "Validate TOTP code and generate access token", description = "Validates a TOTP code along with a temporary token (issued during login when 2FA is active) and returns a permanent access token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "TOTP code validated, access token issued",
                    content = @Content(schema = @Schema(implementation = AccessTokenResponse.class), examples = @ExampleObject(
                            value = """
                                    {
                                      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlcjEyMyIsImlzcyI6ImdsYW5jZV9jaGF0X2JhY2tlbmQiLCJleHAiOjE3ODI4NjI0ODgsImlhdCI6MTc4Mjg1ODg4OCwibmJmIjoxNzgyODU4ODg4LCJpZCI6IjZhNDQzYTgxZDAzZDk2NzRjNjU2MDY2MSIsInNlc3Npb25faWQiOiJmYzY4NzMyZC0wNjFjLTQ4OTktYmVhNC1iNzIyYWQ5YjM5MjIiLCJ0eXBlIjoiQUNDRVNTIn0.nbEoQn4w6EdHHyKV_-vmgFRvRPZFI65bQNh8MEAqeBw"
                                    }
                                    """
                    ))),
            @ApiResponse(responseCode = "400", description = "Validation failed or invalid TOTP code",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class), examples = {
                            @ExampleObject(
                                    name = "Missing Fields Example",
                                    summary = "Example of a missing temporary token and TOTP code response",
                                    value = """
                                            {
                                              "detail": "The code is required",
                                              "instance": "/api/auth/2fa/login",
                                              "status": 400,
                                              "title": "Validation Failed",
                                              "type": "null/validation-failed",
                                              "fieldErrors": {
                                                "temporaryToken": "The token is required",
                                                "totpCode": "The code is required"
                                              }
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Invalid TOTP Code Length Example",
                                    summary = "Example of an invalid TOTP code length response",
                                    value = """
                                            {
                                              "detail": "The code must be exactly 6 digits",
                                              "instance": "/api/auth/2fa/login",
                                              "status": 400,
                                              "title": "Validation Failed",
                                              "type": "null/validation-failed",
                                              "fieldErrors": {
                                                "totpCode": "The code must be exactly 6 digits"
                                              }
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Invalid TOTP Code Example",
                                    summary = "Example of an invalid TOTP code response",
                                    value = """
                                            {
                                              "detail": "Invalid TOTP code provided for user: testuser123",
                                              "instance": "/api/auth/2fa/login",
                                              "status": 400,
                                              "title": "Invalid TOTP Code",
                                              "type": "http://localhost:8080/error/authentication/invalid-totp-code"
                                            }
                                            """
                            )
                    })),
            @ApiResponse(responseCode = "401", description = "Invalid temporary token or two-factor authentication not active",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class), examples =
                    @ExampleObject(
                            name = "Invalid Temporary Token Example",
                            summary = "Example of an invalid temporary token response",
                            value = """
                                    {
                                      "detail": "Invalid temporary token provided",
                                      "instance": "/api/auth/2fa/login",
                                      "status": 401,
                                      "title": "Invalid Temporary Token",
                                      "type": "http://localhost:8080/error/authentication/invalid-temporary-token"
                                    }
                                    """
                    ))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponse> validateTotpCode(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Temporary token and TOTP code for 2FA validation",
                    required = true)
            @RequestBody @Valid TotpCodeWithTemporaryTokenRequest request) {
        var response = this.twoFactorAuthenticationService.verifyTotpCodeAndGenerateAccessToken(request.temporaryToken(), request.totpCode());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Generate 2FA secret", description = "Generates a new TOTP secret for the authenticated user. This secret is used to set up two-factor authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Secret generated successfully",
                    content = @Content(schema = @Schema(implementation = Secret2faResponse.class), examples = @ExampleObject(
                            name = "Secret2faResponse Example",
                            summary = "Example of a successful 2FA secret generation response",
                            value = """
                                    {
                                      "secret": "56QBSEKHXRMY5D5UX77HLORFGCIJ7Q7S"
                                    }
                                    """
                    ))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/generate")
    public ResponseEntity<Secret2faResponse> generateSecret2fa(
            @Parameter(description = "Authenticated user", hidden = true)
            Authentication authentication) {
        IO.println(authentication);
        String secret = this.twoFactorAuthenticationService.generateSecretForActive2fa(authentication.getName());
        return ResponseEntity.ok(new Secret2faResponse(secret));
    }

    @Operation(summary = "Enable two-factor authentication", description = "Validates the first TOTP code and activates two-factor authentication for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Two-factor authentication enabled successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed or invalid TOTP code",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class), examples = {
                            @ExampleObject(
                                    name = "Invalid TOTP Code Example",
                                    summary = "Example of an invalid TOTP code response",
                                    value = """
                                            {
                                              "detail": "Invalid TOTP code provided for user: testuser123",
                                              "instance": "/api/auth/2fa/enable",
                                              "status": 400,
                                              "title": "Invalid TOTP Code",
                                              "type": "http://localhost:8080/error/authentication/invalid-totp-code"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Missing TOTP Code Example",
                                    summary = "Example of a missing TOTP code response",
                                    value = """
                                            {
                                              "detail": "The code is required",
                                              "instance": "/api/auth/2fa/enable",
                                              "status": 400,
                                              "title": "Validation Failed",
                                              "type": "http://localhost:8080/error/validation-failed",
                                              "fieldErrors": {
                                                "value": "The code is required"
                                              }
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Invalid TOTP Code Length Example",
                                    summary = "Example of an invalid TOTP code length response",
                                    value = """
                                            {
                                              "detail": "The code must be exactly 6 digits",
                                              "instance": "/api/auth/2fa/enable",
                                              "status": 400,
                                              "title": "Validation Failed",
                                              "type": "null/validation-failed",
                                              "fieldErrors": {
                                                "value": "The code must be exactly 6 digits"
                                              }
                                            }
                                            """
                            )
                    })),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/enable")
    public ResponseEntity<Void> active2fa(
            @Parameter(description = "Authenticated user", hidden = true)
            Authentication authentication,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "TOTP code to validate for 2FA activation",
                    required = true)
            @RequestBody @Valid TotpCode totpCode) {
        this.twoFactorAuthenticationService.validateTheFirstOtpCodeAndActive2fa(authentication.getName(), totpCode.value());
        return ResponseEntity.noContent().build();
    }
}
