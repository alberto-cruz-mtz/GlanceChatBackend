package alberto.cruz.mtz.glance.chat.backend.service;

import alberto.cruz.mtz.glance.chat.backend.dto.ProfileResponse;
import alberto.cruz.mtz.glance.chat.backend.exception.AvatarUploadException;
import alberto.cruz.mtz.glance.chat.backend.exception.UserNotFoundException;
import alberto.cruz.mtz.glance.chat.backend.model.User;
import alberto.cruz.mtz.glance.chat.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final S3Client clientStorage;
    private final String bucketName;
    private final String bucketUrl;

    @Override
    public ProfileResponse setUpProfile(String id, String username, MultipartFile file) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));

        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            assert contentType != null;
            String extension = contentType.substring(contentType.lastIndexOf("/") + 1);
            String avatarKey = id + "/" + UUID.randomUUID() + "." + extension;

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(this.bucketName)
                    .key(avatarKey)
                    .contentType(contentType)
                    .build();

            try {
                clientStorage.putObject(objectRequest, RequestBody.fromBytes(file.getBytes()));
            } catch (IOException e) {
                throw new AvatarUploadException("Failed to upload avatar file: " + e.getMessage());
            }

            user.setAvatarUrl(this.bucketUrl + "/" + avatarKey);
        }

        if (username != null && !username.isEmpty()) {
            user.setDisplayName(username);
        }

        user.setHasSetUpProfile(true);
        User updatedUser = userRepository.save(user);

        return new ProfileResponse(updatedUser.getAvatarUrl(), updatedUser.getDisplayName());
    }
}
