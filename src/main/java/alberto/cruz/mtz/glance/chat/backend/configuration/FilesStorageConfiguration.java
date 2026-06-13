package alberto.cruz.mtz.glance.chat.backend.configuration;

import com.mongodb.AwsCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class FilesStorageConfiguration {

    @Value("${aws.s3.access-key}")
    private String accessKey;
    @Value("${aws.s3.secret-key}")
    private String secretKey;
    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    @Value("${aws.s3.region}")
    private String region;
    @Value("${aws.s3.url}")
    private String url;
    @Value("${aws.s3.public-url}")
    private String publicUrl;

    @Bean
    public S3Client clientStorage() {
        var credential = AwsBasicCredentials.create(this.accessKey, this.secretKey);
        var credentialsProvider = StaticCredentialsProvider.create(credential);

        return S3Client.builder()
                .endpointOverride(URI.create(this.url))
                .region(Region.of(this.region))
                .credentialsProvider(credentialsProvider)
                .forcePathStyle(true)
                .build();
    }

    @Bean
    public String bucketName() {
        return this.bucketName;
    }

    @Bean
    public String bucketUrl() {
        return this.publicUrl;
    }
}
