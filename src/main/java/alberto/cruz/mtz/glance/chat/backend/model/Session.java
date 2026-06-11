package alberto.cruz.mtz.glance.chat.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "sessions")
@CompoundIndex(name = "active_session_idx", def = "{'user_id': 1, 'is_active': 1}")
public class Session {

    @Id
    private String id;

    @Field("device_name")
    private String deviceName;

    @Field("device_os")
    private String deviceOs;

    @Field("device_fingerprint")
    private String deviceFingerprint;

    @Field("is_active")
    private boolean active;

    @Field("user_id")
    private String userId;

    @Field("created_at")
    private Instant createdAt;
}