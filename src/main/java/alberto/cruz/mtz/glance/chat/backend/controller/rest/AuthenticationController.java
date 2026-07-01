package alberto.cruz.mtz.glance.chat.backend.controller.rest;

import alberto.cruz.mtz.glance.chat.backend.dto.AuthenticationRequest;
import alberto.cruz.mtz.glance.chat.backend.dto.AuthenticationResponse;
import alberto.cruz.mtz.glance.chat.backend.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
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

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with displayName and password. Returns an access token upon successful registration.",
            method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User credentials for registration",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = AuthenticationRequest.class),
                            examples = @ExampleObject(
                                    name = "Example Registration",
                                    summary = "Example of user registration request",
                                    value = """
                                            {
                                              "displayName": "test_user123",
                                              "password": "TestPassword@123"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticationResponse.class),
                            examples = @ExampleObject(
                                    name = "Respuesta Exitosa",
                                    summary = "Ejemplo de usuario retornado",
                                    value = """
                                            {
                                              "id": "6a443a81d03d9674c6560661",
                                              "publicId": "QE-EY-22-TS",
                                              "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlcjEyMyIsImlzcyI6ImdsYW5jZV9jaGF0X2JhY2tlbmQiLCJleHAiOjE3ODI4NTk5MjEsImlhdCI6MTc4Mjg1NjMyMSwibmJmIjoxNzgyODU2MzIxLCJpZCI6IjZhNDQzYTgxZDAzZDk2NzRjNjU2MDY2MSIsInNlc3Npb25faWQiOiIwNmIxZTJlNy0yYjM5LTQwMGMtODI5OS04ODVhZDcwYjMwMzYiLCJ0eXBlIjoiQUNDRVNTIn0.JnNWcNS1qtqcZIV3hcp9LYEFgEWtUcr7fFVzt9RWmJA",
                                              "avatar": null,
                                              "displayName": "testuser123",
                                              "hasSetUpProfile": false,
                                              "requiredAuthenticateFor2FA": false
                                            }"""
                            ))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class),
                            examples = {@ExampleObject(
                                    name = "Error de Validación por campos vacios",
                                    summary = "Ejemplo de error de validación por campos vacios",
                                    value = """
                                            {
                                              "detail": "The displayName is required",
                                              "instance": "/api/auth/signup",
                                              "status": 400,
                                              "title": "Validation Failed",
                                              "type": "null/validation-failed",
                                              "fieldErrors": {
                                                "password": "The password is required",
                                                "displayName": "The displayName is required"
                                              }
                                            }
                                            """
                            ),
                                    @ExampleObject(
                                            name = "Error de Validación por campos que no cumplen con el patrón",
                                            summary = "Ejemplo de error de validación por campos que no cumplen con el patrón",
                                            value = """
                                                    {
                                                      "detail": "The displayName can only contain letters, numbers and underscores",
                                                      "instance": "/api/auth/signup",
                                                      "status": 400,
                                                      "title": "Validation Failed",
                                                      "type": "null/validation-failed",
                                                      "fieldErrors": {
                                                        "password": "The password must be at least 8 characters long and contain at least one letter and one number",
                                                        "displayName": "The displayName can only contain letters, numbers and underscores"
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Error de Validación por longitud de los campos",
                                            summary = "Ejemplo de error de validación por longitud de los campos",
                                            value = """
                                                    {
                                                      "detail": "The password must be at least 8 characters long",
                                                      "instance": "/api/auth/signup",
                                                      "status": 400,
                                                      "title": "Validation Failed",
                                                      "type": "null/validation-failed",
                                                      "fieldErrors": {
                                                        "password": "The password must be at least 8 characters long",
                                                        "displayName": "The displayName must be between 5 and 25 characters long"
                                                      }
                                                    }
                                                    """
                                    ),
                            }
                    )),
            @ApiResponse(responseCode = "409", description = "Username already in use",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class, example = "{\"type\":\"https://example.com/username-already-in-use\",\"title\":\"Username already in use\",\"status\":409,\"detail\":\"The displayName is already taken. Please choose a different one.\"}"),
                            examples = @ExampleObject(
                                    name = "Error de Usuario ya existente",
                                    summary = "Ejemplo de error de usuario ya existente",
                                    value = """
                                            {
                                              "detail": "Username is already in use: testuser123",
                                              "instance": "/api/auth/signup",
                                              "status": 409,
                                              "title": "Email Already In Use",
                                              "type": "http://localhost:8080/error/authentication/email-already-in-use"
                                            }
                                            """
                            )))
    })
    @PostMapping("/signup")
    public ResponseEntity<AuthenticationResponse> registerNewUser(
            @RequestBody @Valid AuthenticationRequest request) {
        var response = this.authenticationService.register(request.username(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Authenticate a user",
            description = "Authenticates a user with displayName and password. If two-factor authentication is enabled, a temporary token is returned instead of an access token.",
            method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User credentials for authentication",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = AuthenticationRequest.class),
                            examples = @ExampleObject(
                                    name = "Example Login",
                                    summary = "Example of user login",
                                    value = """
                                            {
                                              "displayName": "test_user123",
                                              "password": "TestPassword@123"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User authenticated successfully",
                    content = @Content(schema = @Schema(implementation = AuthenticationResponse.class), examples = {
                            @ExampleObject(
                                    name = "Respuesta Exitosa",
                                    summary = "Ejemplo de usuario retornado",
                                    value = """
                                            {
                                              "id": "6a443a81d03d9674c6560661",
                                              "publicId": "QE-EY-22-TS",
                                              "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlcjEyMyIsImlzcyI6ImdsYW5jZV9jaGF0X2JhY2tlbmQiLCJleHAiOjE3ODI4NjA2NTAsImlhdCI6MTc4Mjg1NzA1MCwibmJmIjoxNzgyODU3MDUwLCJpZCI6IjZhNDQzYTgxZDAzZDk2NzRjNjU2MDY2MSIsInNlc3Npb25faWQiOiI5MzFlMGZhNi00YWI4LTQyMGQtYjYwZi0wNDEwNzA1MWFmODciLCJ0eXBlIjoiQUNDRVNTIn0.NNWLYzTQFZE89bQz4iX7vWW2MTerugDiL1OG3A738gQ",
                                              "avatar": null,
                                              "displayName": "testuser123",
                                              "hasSetUpProfile": false,
                                              "requiredAuthenticateFor2FA": false
                                            }
                                            """
                            )
                    })),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class),
                            examples = {@ExampleObject(
                                    name = "Error de Validación por campos vacios",
                                    summary = "Ejemplo de error de validación por campos vacios",
                                    value = """
                                            {
                                              "detail": "The displayName is required",
                                              "instance": "/api/auth/signup",
                                              "status": 400,
                                              "title": "Validation Failed",
                                              "type": "null/validation-failed",
                                              "fieldErrors": {
                                                "password": "The password is required",
                                                "displayName": "The displayName is required"
                                              }
                                            }
                                            """
                            ),
                                    @ExampleObject(
                                            name = "Error de Validación por campos que no cumplen con el patrón",
                                            summary = "Ejemplo de error de validación por campos que no cumplen con el patrón",
                                            value = """
                                                    {
                                                      "detail": "The displayName can only contain letters, numbers and underscores",
                                                      "instance": "/api/auth/signup",
                                                      "status": 400,
                                                      "title": "Validation Failed",
                                                      "type": "null/validation-failed",
                                                      "fieldErrors": {
                                                        "password": "The password must be at least 8 characters long and contain at least one letter and one number",
                                                        "displayName": "The displayName can only contain letters, numbers and underscores"
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Error de Validación por longitud de los campos",
                                            summary = "Ejemplo de error de validación por longitud de los campos",
                                            value = """
                                                    {
                                                      "detail": "The password must be at least 8 characters long",
                                                      "instance": "/api/auth/signup",
                                                      "status": 400,
                                                      "title": "Validation Failed",
                                                      "type": "null/validation-failed",
                                                      "fieldErrors": {
                                                        "password": "The password must be at least 8 characters long",
                                                        "displayName": "The displayName must be between 5 and 25 characters long"
                                                      }
                                                    }
                                                    """
                                    ),
                            }
                    )),
            @ApiResponse(responseCode = "403", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(
                            name = "Error de Credenciales Invalidas",
                            summary = "Ejemplo de error de credenciales invalidas",
                            value = """
                                    {
                                      "detail": "Credentials incorrect, Please verify your credentials",
                                      "instance": "/api/auth/login",
                                      "status": 403,
                                      "title": "Authentication failed",
                                      "type": "http://localhost:8080/error/authentication"
                                    }
                                    """
                    )))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticateUser(
            @RequestBody @Valid AuthenticationRequest request) {
        var response = this.authenticationService.authenticate(request.username(), request.password());
        return ResponseEntity.ok(response);
    }
}
