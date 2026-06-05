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

    /**
     * Email único — sirve como identificador de login.
     */
    @Indexed(unique = true)
    private String email;

    /**
     * Nombre visible en conversaciones (username / display name).
     */
    @Indexed
    @Field("display_name")
    private String displayName;

    /**
     * URL pública del avatar. Puede ser null.
     */
    @Field("avatar_url")
    private String avatarUrl;

    /**
     * Presencia actual del usuario.
     */
    @Builder.Default
    private UserStatus status = UserStatus.OFFLINE;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private Instant updatedAt;
}