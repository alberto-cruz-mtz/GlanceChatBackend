package alberto.cruz.mtz.glance.chat.backend.controller.rest;

import alberto.cruz.mtz.glance.chat.backend.dto.AuthorizeDeviceRequest;
import alberto.cruz.mtz.glance.chat.backend.dto.DeviceCodeRequest;
import alberto.cruz.mtz.glance.chat.backend.dto.DeviceCodeResponse;
import alberto.cruz.mtz.glance.chat.backend.service.DeviceAuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/auth/devices")
@RequiredArgsConstructor
public class DeviceAuthorizationController {

    private final DeviceAuthorizationService deviceAuthorizationService;

    @Operation(
            summary = "Request a device code for device authorization",
            description = "Generates a device code that can be used to authorize a device for a user. The device code is valid for a limited time and can be checked for authorization status.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Device code request payload",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = DeviceCodeRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "deviceName": "My Device",
                                                "osVersion": "1.0.0"
                                            }
                                            """
                            )
                    )
            ),
            method = "POST",
            tags = {"Device Authorization", "Permit All"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Device code generated successfully",
            content = @Content(
                    contentSchema = @Schema(implementation = DeviceCodeResponse.class),
                    examples = @ExampleObject(
                            name = "DeviceCodeResponseExample",
                            summary = "Example response for a successful device code generation",
                            value = """
                                    {
                                      "deviceCode": "262477",
                                      "expiresIn": 300
                                    }
                                    """
                    )
            )
    )
    @PostMapping("/request-code")
    public ResponseEntity<DeviceCodeResponse> getDeviceCode(@RequestBody @Valid DeviceCodeRequest request) {
        var response = this.deviceAuthorizationService.generateDeviceCode(request.deviceName(), request.osVersion());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Check the status of a device code",
            description = "Checks whether a device code has been authorized or is still pending. If the device code is authorized, it returns an authentication response; otherwise, it returns an accepted status indicating that the device code is still pending.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Device code request payload",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = AuthorizeDeviceRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            { "deviceCode": "412025" }
                                            """
                            ))
            ),
            method = "POST",
            tags = {"Device Authorization", "Permit All"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "Device code is still pending authorization. The client should continue polling for the status.",
                    content = @Content(
                            schema = @Schema(type = "null"),
                            examples = @ExampleObject(
                                    name = "PendingDeviceCodeExample",
                                    summary = "Example response for a pending device code",
                                    value = "{}"
                            )
                    )),
            @ApiResponse(
                    responseCode = "200",
                    description = "Device code has been authorized. Returns an authentication response containing user information and a token.",
                    content = @Content(
                            schema = @Schema(implementation = alberto.cruz.mtz.glance.chat.backend.dto.AuthenticationResponse.class),
                            examples = @ExampleObject(
                                    name = "AuthorizedDeviceCodeExample",
                                    summary = "Example response for an authorized device code",
                                    value = """
                                            {
                                              "id": "6a443a81d03d9674c6560661",
                                              "publicId": "QE-EY-22-TS",
                                              "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlcjEyMyIsImlzcyI6ImdsYW5jZV9jaGF0X2JhY2tlbmQiLCJleHAiOjE3ODI4NjA3MDQsImlhdCI6MTc4Mjg2MDQwNCwibmJmIjoxNzgyODYwNDA0LCJpZCI6IjZhNDQzYTgxZDAzZDk2NzRjNjU2MDY2MSIsInR5cGUiOiIyRkEiLCJzY29wZSI6IjJGQV9PTkxZIn0.UhnLHTQWLJyH6pGpG6_lXcQ1hiJMxv_RuDveY_V7Vsg",
                                              "avatar": null,
                                              "username": "testuser123",
                                              "hasSetUpProfile": false,
                                              "requiredAuthenticateFor2FA": true
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid device code format. The device code must be a 6-digit number and cannot be empty.",
                    content = @Content(
                            schema = @Schema(type = "null"),
                            examples = {
                                    @ExampleObject(
                                            name = "InvalidDeviceCodeFormatExample",
                                            summary = "Example response for an invalid device code format",
                                            value = """
                                                    {
                                                      "detail": "Device code must not be empty",
                                                      "instance": "/api/auth/devices/checked",
                                                      "status": 400,
                                                      "title": "Validation Failed",
                                                      "type": "http://localhost:8080/validation-failed",
                                                      "fieldErrors": {
                                                        "deviceCode": "Device code must not be empty"
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "InvalidDeviceCodeFormatExample2",
                                            summary = "Example response for an invalid device code format (not a 6-digit number)",
                                            value = """
                                                    {
                                                      "detail": "Invalid device code format. It must be a 6-digit number.",
                                                      "instance": "/api/auth/devices/checked",
                                                      "status": 400,
                                                      "title": "Validation Failed",
                                                      "type": "http://localhost:8080/validation-failed",
                                                      "fieldErrors": {
                                                        "deviceCode": "Invalid device code format. It must be a 6-digit number."
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "InvalidOrExpiredDeviceCodeExample",
                                            summary = "Example response for an invalid or expired device code",
                                            value = """
                                                    {
                                                      "detail": "The device code is invalid or has expired. Please request a new device code.",
                                                      "instance": "/api/auth/devices/checked",
                                                      "status": 400,
                                                      "title": "Invalid or Expired Device Code",
                                                      "type": "http://localhost:8080/error/authentication/invalid-or-expired-device-code"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @PostMapping("/checked")
    public ResponseEntity<?> authenticateUserWithDeviceCode(@RequestBody @Valid AuthorizeDeviceRequest request) {
        var response = this.deviceAuthorizationService.checkDeviceCodeStatus(request.deviceCode());

        if (response.isEmpty()) return ResponseEntity.accepted().build();

        return ResponseEntity.ok(response.get());
    }

    @Operation(
            summary = "Authorize a device for a user",
            description = "Authorizes a device for a user based on the provided device code. This endpoint is typically called after the user has authenticated and approved the device authorization request.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Device authorization request payload",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = AuthorizeDeviceRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            { "deviceCode": "412025" }
                                            """
                            )
                    )),
            tags = {"Device Authorization", "Requires Authentication"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Device authorized successfully. No content is returned in the response.",
                    content = @Content(
                            schema = @Schema(type = "null"),
                            examples = @ExampleObject(
                                    name = "DeviceAuthorizedExample",
                                    summary = "Example response for a successful device authorization",
                                    value = "{}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid device code format or the device code is invalid/expired. The device must be a 6-digit number and cannot be empty.",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = {
                                    @ExampleObject(
                                            name = "InvalidDeviceCodeFormatExample",
                                            summary = "Example response for an invalid device code format",
                                            value = """
                                                    {
                                                      "detail": "The device code is invalid or has expired. Please request a new device code.",
                                                      "instance": "/api/auth/devices/authorize",
                                                      "status": 400,
                                                      "title": "Invalid or Expired Device Code",
                                                      "type": "http://localhost:8080/error/authentication/invalid-or-expired-device-code"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "EmptyDeviceCodeExample",
                                            summary = "Example response for an empty device code",
                                            value = """
                                                    {
                                                      "detail": "Device code must not be empty",
                                                      "instance": "/api/auth/devices/authorize",
                                                      "status": 400,
                                                      "title": "Validation Failed",
                                                      "type": "http://localhost:8080/validation-failed",
                                                      "fieldErrors": {
                                                        "deviceCode": "Device code must not be empty"
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "InvalidDeviceCodeFormatExample2",
                                            summary = "Example response for an invalid device code format (not a 6-digit number)",
                                            value = """
                                                    {
                                                      "detail": "Invalid device code format. It must be a 6-digit number.",
                                                      "instance": "/api/auth/devices/authorize",
                                                      "status": 400,
                                                      "title": "Validation Failed",
                                                      "type": "http://localhost:8080/validation-failed",
                                                      "fieldErrors": {
                                                        "deviceCode": "Invalid device code format. It must be a 6-digit number."
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @PostMapping("/authorize")
    public ResponseEntity<Void> authorizeDevice(@RequestBody @Valid AuthorizeDeviceRequest request, Authentication authentication) {
        String username = authentication.getName();
        this.deviceAuthorizationService.authorizeDevice(request.deviceCode(), username);
        return ResponseEntity.noContent().build();
    }
}