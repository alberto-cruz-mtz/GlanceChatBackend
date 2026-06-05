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
@Document(collection = "sessions")
@CompoundIndexes({
        @CompoundIndex(name = "idx_user_active", def = "{'user_id': 1, 'is_active': 1}"),
        @CompoundIndex(name = "idx_device_fingerprint", def = "{'device_fingerprint': 1}")
})
public class Session {

    @Id
    private String id;

    /**
     * Usuario dueño de la sesión.
     */
    @Field("user_id")
    private String userId;

    /**
     * Hash SHA-256 del bearer token enviado al cliente.
     * El cliente guarda el token raw; el servidor nunca lo almacena en claro.
     * En cada request: SHA-256(tokenEntrante) → buscar en esta colección.
     */
    @Indexed(unique = true)
    @Field("token")
    private String tokenHash;

    /**
     * Huella única del dispositivo.
     * Se construye en el servidor a partir de:
     * SHA-256(User-Agent + Accept-Language + IP + [UUID de app en móvil])
     * <p>
     * Si el token llega desde un fingerprint distinto → revocar sesión.
     */
    @Field("device_fingerprint")
    private String deviceFingerprint;

    /**
     * Nombre legible del dispositivo (ej: "iPhone de Juan"). Puede ser null.
     */
    @Field("device_name")
    private String deviceName;

    /**
     * Sistema operativo del dispositivo (ej: "iOS 17.4"). Puede ser null.
     */
    @Field("device_os")
    private String deviceOS;

    /**
     * IP en el momento de crear la sesión. Puede ser null.
     */
    @Field("ip_address")
    private String ipAddress;

    /**
     * False cuando la sesión es cerrada manualmente o revocada por seguridad.
     */
    @Builder.Default
    @Field("is_active")
    private boolean isActive = true;

    /**
     * Última vez que este token fue utilizado.
     * Actualizar en cada request autenticado para tener actividad reciente.
     */
    @Field("last_active_at")
    private Instant lastActiveAt;

    /**
     * Expiración absoluta de la sesión.
     * TTL index elimina el documento al llegar a esta fecha.
     * Valor recomendado: Instant.now().plus(30, ChronoUnit.DAYS).
     */
    @Indexed(expireAfter = "10d")
    @Field("expires_at")
    private Instant expiresAt;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;
}