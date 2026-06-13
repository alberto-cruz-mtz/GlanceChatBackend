package alberto.cruz.mtz.glance.chat.backend.controller.rest;

import alberto.cruz.mtz.glance.chat.backend.dto.ProfileRequest;
import alberto.cruz.mtz.glance.chat.backend.dto.ProfileResponse;
import alberto.cruz.mtz.glance.chat.backend.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileResponse> customizeProfile(
            @RequestPart(name = "avatar", required = false) MultipartFile file,
            @RequestPart(name = "profile") @Valid ProfileRequest request
    ) {
        var response = this.profileService.setUpProfile(request.id(), request.username(), file);
        return ResponseEntity.ok(response);
    }

    @PatchMapping(path = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateAvatar(
            @RequestPart(name = "avatar") MultipartFile file,
            @RequestPart(name = "id") String id
    ) {
        var avatarUrl = this.profileService.updateAvatarUrl(id, file);
        return ResponseEntity.ok(avatarUrl);
    }

    @PatchMapping("/username")
    public ResponseEntity<Void> updateUsername(@RequestBody @Valid ProfileRequest request) {
        this.profileService.updateUsername(request.id(), request.username());
        return ResponseEntity.noContent().build();
    }
}
