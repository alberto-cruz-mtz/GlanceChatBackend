package alberto.cruz.mtz.glance.chat.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String password;

    @Indexed(unique = true)
    @Field("public_id")
    private String publicId;

    @Field("display_name")
    private String displayName;

    @Field("avatar_url")
    private String avatarUrl;

    @Field("secret_user_2fa")
    private String secret;

    @Builder.Default
    private UserStatus status = UserStatus.OFFLINE;

    @Field("has_setup_up_profile")
    @Builder.Default
    private boolean hasSetUpProfile = false;

    @Field("enabled_2fa")
    @Builder.Default
    private boolean enabled2fa = false;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private Instant updatedAt;

    public static User create(String username, String password, String publicId) {
        return User.builder()
                .username(username)
                .password(password)
                .publicId(publicId)
                .displayName(username.split("@")[0]) // Por defecto, el display name es la parte antes del @
                .build();
    }
}