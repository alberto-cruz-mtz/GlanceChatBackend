package alberto.cruz.mtz.glance.chat.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "messages")
@CompoundIndex(name = "conversation_history_idx", def = "{'conversation_id': 1, 'sent_at': -1}")
public class Message {

    @Id
    private String id;

    /**
     * Conversación a la que pertenece este mensaje.
     */
    @Field("conversation_id")
    private String conversationId;

    /**
     * Usuario que envió el mensaje.
     */
    @Field("sender_id")
    private String senderId;

    /**
     * Contenido del mensaje en texto plano.
     * Máximo 4096 caracteres.
     * Null cuando el mensaje ha sido eliminado (ver deletedAt).
     */
    private String content;

    /**
     * Estado de lectura del mensaje.
     * Transiciones válidas: SENT → DELIVERED → READ.
     */
    @Builder.Default
    private MessageStatus status = MessageStatus.SENT;

    /**
     * Fecha y hora exacta en que el servidor persistió el mensaje.
     * Se establece en la capa de servicio antes de guardar.
     */
    @Field("sent_at")
    private Instant sentAt;

    /**
     * Cuando el mensaje fue entregado al dispositivo del destinatario.
     * Null hasta que se confirme la entrega (ej: ACK de WebSocket/push).
     */
    @Field("delivered_at")
    private Instant deliveredAt;

    /**
     * Marca de borrado suave.
     * Si no es null, el mensaje fue eliminado por el remitente.
     * Al eliminar: establecer deletedAt = Instant.now() y limpiar content = null.
     */
    @Field("deleted_at")
    private Instant deletedAt;
}