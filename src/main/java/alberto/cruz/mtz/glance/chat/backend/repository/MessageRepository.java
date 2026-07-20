package alberto.cruz.mtz.glance.chat.backend.repository;

import alberto.cruz.mtz.glance.chat.backend.model.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {

    @Query(value = "{conversation_ids: ?0}", sort = "{sent_at: -1}")
    List<Message> findByConversationIds(String conversationIds, Pageable pageable);

    @Query(value = "{conversation_ids: ?0, sent_at: {$lt: ?1}}", sort = "{sent_at: -1}")
    List<Message> findOlderByConversationIds(String conversationIds, Instant before, Pageable pageable);
}
