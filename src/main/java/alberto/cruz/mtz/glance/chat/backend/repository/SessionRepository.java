package alberto.cruz.mtz.glance.chat.backend.repository;

import alberto.cruz.mtz.glance.chat.backend.model.Session;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SessionRepository extends MongoRepository<Session, String> {
}
