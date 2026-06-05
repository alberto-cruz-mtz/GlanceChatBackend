package alberto.cruz.mtz.glance.chat.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
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

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "auth_codes")
@CompoundIndexes({
        @CompoundIndex(name = "idx_user_used", def = "{'user_id': 1, 'used': 1}"),
        @CompoundIndex(name = "idx_email_date", def = "{'email': 1, 'created_at': -1}")
})
public class AuthenticationCode {

    @Id
    private String id;

    /**
     * Referencia al usuario que solicitó el código.
     */
    @Field("user_id")
    private String userId;

    /**
     * Email al que se envió el código (snapshot en el momento del envío).
     */
    private String email;

    /**
     * Hash del código OTP.
     * Calcular como: SHA-256(code + salt) o BCrypt.
     * Nunca almacenar el código en texto plano.
     */
    @Field("code_hash")
    private String codeHash;

    /**
     * True una vez que el código fue verificado exitosamente.
     */
    @Builder.Default
    private boolean used = false;

    /**
     * Contador de intentos fallidos de verificación.
     * Si llega a 5 el código debe ser invalidado (marcar used = true).
     */
    @Builder.Default
    private int attempts = 0;

    /**
     * Expiración absoluta del código.
     * Spring Data + TTL index eliminan el documento al llegar a esta fecha.
     * Valor recomendado: Instant.now().plusSeconds(600) → 10 minutos.
     */
    @Indexed(expireAfter = "600s")
    @Field("expires_at")
    private Instant expiresAt;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    /**
     * IP desde donde se solicitó el código. Puede ser null.
     */
    @Field("ip_address")
    private String ipAddress;
}
