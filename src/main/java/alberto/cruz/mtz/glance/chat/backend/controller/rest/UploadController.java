package alberto.cruz.mtz.glance.chat.backend.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    @Value("${aws.s3.bucket-name-chat}")
    public String bucket;

    @Value("${aws.s3.public-url-chat}")
    public String publicDomain;

    private final S3Presigner s3Presigner;

    // DTO interno para recibir los datos del frontend
    public static class UploadDto {
        public String fileName;
        public String fileType;
    }

    @PostMapping("/presigned-url")
    public ResponseEntity<Map<String, String>> getPresignedUrl(@RequestBody UploadDto request) {
        // Evitar colisiones de nombres con un timestamp
        String uniqueKey = "chat-media/" + System.currentTimeMillis() + "-" + request.fileName;

        // 1. Crear la petición de objeto definiendo el Content-Type
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(uniqueKey)
                .contentType(request.fileType)
                .build();

        // 2. Crear la petición de firma (válida por 1 minuto)
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(1))
                .putObjectRequest(objectRequest)
                .build();

        // 3. Generar la URL
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        String uploadUrl = presignedRequest.url().toString();
        String publicUrl = publicDomain + "/" + uniqueKey;

        // 4. Devolver las URLs al frontend
        Map<String, String> response = new HashMap<>();
        response.put("uploadUrl", uploadUrl);
        response.put("publicUrl", publicUrl);
        response.put("key", uniqueKey);

        return ResponseEntity.ok(response);
    }
}
