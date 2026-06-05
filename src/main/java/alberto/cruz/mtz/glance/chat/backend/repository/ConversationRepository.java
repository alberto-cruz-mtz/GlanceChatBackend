package alberto.cruz.mtz.glance.chat.backend.repository;

import alberto.cruz.mtz.glance.chat.backend.model.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ConversationRepository extends MongoRepository<Conversation, String> {

    List<Conversation> findByParticipantIds(List<String> participantIds);

    @Query(value = "{participants_id: ?0}", sort = "{last_message_at: -1}")
    List<Conversation> findByParticipantIds(String participantId);
}
