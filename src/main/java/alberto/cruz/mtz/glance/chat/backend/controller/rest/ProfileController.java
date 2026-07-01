package alberto.cruz.mtz.glance.chat.backend.controller.rest;

import alberto.cruz.mtz.glance.chat.backend.dto.AvatarResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.ProfileRequest;
import alberto.cruz.mtz.glance.chat.backend.dto.ProfileResponse;
import alberto.cruz.mtz.glance.chat.backend.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @Operation(
            summary = "Customize the profile of the authenticated user",
            description = "This endpoint allows the authenticated user to customize their profile by providing a display name and an optional avatar image. The request must contain a valid display name and can include an image file in multipart/form-data format.",
            parameters = {
                    @Parameter(description = "Multipart file containing the avatar image (optional)", name = "avatar", required = false),
                    @Parameter(description = "ProfileRequest object containing the display name", name = "profile", required = true),
            },
            tags = {"Profile", "Requires Authentication", "Multipart Form Data"},
            security = {@SecurityRequirement(name = "Bearer Authentication")}
    )
    @ApiResponses({})
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileResponse> customizeProfile(
            @RequestPart(name = "avatar", required = false) MultipartFile file,
            @RequestPart(name = "profile") @Valid ProfileRequest request,
            Authentication authentication
    ) {
        String userId = Objects.requireNonNull(authentication.getCredentials()).toString();
        var response = this.profileService.setUpProfile(userId, request.displayName(), file);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update the avatar of the authenticated user",
            description = "This endpoint allows the authenticated user to update their avatar. The request must contain a valid image file in multipart/form-data format.",
            parameters = {
                    @Parameter(description = "Multipart file containing the new avatar image", name = "avatar", required = true),
                    @Parameter(description = "Authentication object containing the credentials of the authenticated user", name = "authentication", required = true)
            },
            tags = {"Profile", "Requires Authentication", "Multipart Form Data"},
            method = "PATCH",
            security = {@SecurityRequirement(name = "Bearer Authentication")}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Avatar updated successfully. The response body contains the URL of the updated avatar.",
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = AvatarResponse.class),
                            examples = @ExampleObject(
                                    name = "Avatar Update Success Example",
                                    summary = "An example of a successful avatar update response",
                                    value = """
                                            {
                                                "avatar": "http://localhost:9000/avatars/6a443a81d03d9674c6560661/f3e58ff7-b541-4c25-b2ed-8e0bf33bba9e.jpeg"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request. The avatar file is missing or not in the correct format.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(
                                    name = "Missing Avatar Example",
                                    summary = "An example of a request with a missing avatar file",
                                    value = """
                                            {
                                              "detail": "Required part 'avatar' is not present.",
                                              "instance": "/api/profiles/avatar",
                                              "status": 400,
                                              "title": "Bad Request"
                                            }
                                            """
                            )
                    )
            )
    })
    @PatchMapping(path = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AvatarResponse> updateAvatar(
            @RequestPart(name = "avatar") MultipartFile file,
            Authentication authentication
    ) {
        String userId = Objects.requireNonNull(authentication.getCredentials()).toString();
        var avatarUrl = this.profileService.updateAvatarUrl(userId, file);
        return ResponseEntity.ok(new AvatarResponse(avatarUrl));
    }

    @Operation(
            summary = "Update the display name of the authenticated user",
            description = "This endpoint allows the authenticated user to update their display name. The request body must contain a valid display name that meets the specified constraints.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "ProfileRequest object containing the new display name",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProfileRequest.class),
                            examples = @ExampleObject(
                                    name = "Valid Display Name Example",
                                    summary = "An example of a valid display name",
                                    value = """
                                            {
                                              "displayName": "Test user fake"
                                            }
                                            """
                            )
                    )
            ),
            tags = {"Profile", "Requires Authentication"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Display name updated successfully. No content is returned in the response body.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Void.class),
                            examples = @ExampleObject(
                                    name = "No Content Response Example",
                                    summary = "An example of a successful response with no content"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request. The display name does not meet the specified constraints.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Invalid Display Name Example",
                                            summary = "An example of an invalid display name that does not meet the constraints",
                                            value = """
                                                    {
                                                      "detail": "The displayName must be between 5 and 50 characters",
                                                      "instance": "/api/profiles/displayName",
                                                      "status": 400,
                                                      "title": "Validation Failed",
                                                      "type": "http://localhost:8080/error/validation-failed",
                                                      "fieldErrors": {
                                                        "displayName": "The displayName must be between 5 and 50 characters"
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Missing Display Name Example",
                                            summary = "An example of a request with a missing display name",
                                            value = """
                                                    {
                                                      "detail": "The displayName is required",
                                                      "instance": "/api/profiles/displayName",
                                                      "status": 400,
                                                      "title": "Validation Failed",
                                                      "type": "http://localhost:8080/error/validation-failed",
                                                      "fieldErrors": {
                                                        "displayName": "The displayName is required"
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @PatchMapping("/displayName")
    public ResponseEntity<Void> updateUsername(@RequestBody @Valid ProfileRequest request, Authentication authentication) {
        String userId = Objects.requireNonNull(authentication.getCredentials()).toString();
        this.profileService.updateUsername(userId, request.displayName());
        return ResponseEntity.noContent().build();
    }
}
