package alberto.cruz.mtz.glance.chat.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "conversations")
public class Conversation {

    @Id
    private String id;

    @Field("sender_id")
    private String senderId;

    @Field("recipient_id")
    private String recipientId;

    @Field("last_message")
    private LastMessage lastMessage;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;
}