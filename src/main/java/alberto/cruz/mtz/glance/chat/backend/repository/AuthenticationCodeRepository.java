package alberto.cruz.mtz.glance.chat.backend.repository;

import alberto.cruz.mtz.glance.chat.backend.model.AuthenticationCode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AuthenticationCodeRepository extends MongoRepository<AuthenticationCode, String> {

    /**
     * Busca un código válido por userId: no usado, no expirado y con intentos
     * disponibles. Se usa para verificar el OTP ingresado por el usuario.
     */
    @Query("{user_id: ?0, used: false, expires_at: {$gt: ?1}, attempts: {$lt: 5}}")
    Optional<AuthenticationCode> findAvailableCode(String userId, Instant now);

    @Query("{user_id: ?0, used: false}")
    List<AuthenticationCode> findAllCodesNotUsedByUserId(String userId);
}
