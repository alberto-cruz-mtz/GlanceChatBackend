package alberto.cruz.mtz.glance.chat.backend.model;

import lombok.AllArgsConstructor;
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
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "conversations")
@CompoundIndex(name = "user_inbox_idx", def = "{'participants': 1, 'last_message.sent_at': -1}")
public class Conversation {

    @Id
    private String id;

    @Indexed(unique = true, name = "idx_participants_unique")
    @Field("participants")
    private List<String> participantsId;

    @Field("last_message")
    private LastMessage lastMessage;

    @Field("created_at")
    private Instant createdAt;
}
