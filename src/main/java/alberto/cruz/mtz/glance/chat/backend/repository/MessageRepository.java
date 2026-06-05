package alberto.cruz.mtz.glance.chat.backend.repository;

import alberto.cruz.mtz.glance.chat.backend.model.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<Message, String> {

    Slice<Message> findByConversationId(String conversationId, Pageable pageable);

}
