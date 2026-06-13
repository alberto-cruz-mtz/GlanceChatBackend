package alberto.cruz.mtz.glance.chat.backend.util;

import alberto.cruz.mtz.glance.chat.backend.exception.AvatarUploadException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AvatarStorage {

    private final S3Client clientStorage;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.public-url}")
    private String bucketUrl;

    public String upload(String userId, MultipartFile file) {
        String contentType = file.getContentType();
        assert contentType != null;

        String extension = this.getExtension(contentType);
        String avatarKey = this.generateAvatarKey(userId, extension);

        PutObjectRequest objectRequest = this.buildObjectRequest(avatarKey, contentType);
        this.uploadToStorage(objectRequest, file);

        return this.bucketUrl + "/" + avatarKey;
    }

    private String getExtension(String contentType) {
        return contentType.substring(contentType.lastIndexOf("/") + 1);
    }

    private String generateAvatarKey(String userId, String extension) {
        return userId + "/" + UUID.randomUUID() + "." + extension;
    }

    private PutObjectRequest buildObjectRequest(String avatarKey, String contentType) {
        return PutObjectRequest.builder()
                .bucket(this.bucketName)
                .key(avatarKey)
                .contentType(contentType)
                .build();
    }

    private void uploadToStorage(PutObjectRequest objectRequest, MultipartFile file) {
        try {
            clientStorage.putObject(objectRequest, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            throw new AvatarUploadException("Failed to upload avatar file: " + e.getMessage());
        }
    }
}
