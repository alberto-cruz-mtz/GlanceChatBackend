package alberto.cruz.mtz.glance.chat.backend.repository;

import alberto.cruz.mtz.glance.chat.backend.model.Conversation;
import alberto.cruz.mtz.glance.chat.backend.model.Session;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends MongoRepository<Session, String> {

    /**
     * Validación de token en cada request autenticado.
     * Verifica simultáneamente: hash del token, fingerprint del dispositivo,
     * sesión activa y que no haya expirado.
     * Si el tokenHash coincide pero el deviceFingerprint no → sesión comprometida.
     * En ese caso el servicio debe invocar revokeAllByUserId() como medida de seguridad.
     */
    @Query("{token: ?0, device_fingerprint: ?1, is_active: true, expires_at: {$gt: ?2}}")
    Optional<Session> findSessionValid(String tokenHash, String deviceFingerprint, Instant expiresAtBefore);

    @Query("{user_id: ?0, is_active: true, expires_at: {$gt: ?1}}")
    List<Session> findAllSessionsActivesByUserId(String userId, Instant now);
}
