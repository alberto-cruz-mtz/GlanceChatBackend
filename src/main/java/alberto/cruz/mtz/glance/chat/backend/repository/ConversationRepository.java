package alberto.cruz.mtz.glance.chat.backend.repository;

import alberto.cruz.mtz.glance.chat.backend.model.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ConversationRepository extends MongoRepository<Conversation, String> {

    @Query(value = "{sender_id: ?0}", sort = "{'last_message.sent_at': -1}")
    List<Conversation> findBySenderId(String senderId);

    boolean existsBySenderIdAndRecipientId(String senderId, String recipientId);
}
