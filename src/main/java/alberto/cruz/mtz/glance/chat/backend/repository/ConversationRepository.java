package alberto.cruz.mtz.glance.chat.backend.repository;

import alberto.cruz.mtz.glance.chat.backend.model.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ConversationRepository extends MongoRepository<Conversation, String> {

    @Query(value = "{participants: ?0}", sort = "{last_message.sentAt: -1}")
    List<Conversation> findByParticipantIds(String participantId);
}
