package alberto.cruz.mtz.glance.chat.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

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
     * Conversaciones a los que pertenece el mensaje
     */
    @Field("conversation_ids")
    private List<String> conversationIds;

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
     * Metadatos dinámicos para archivos multimedia o documentos.
     * Será nulo si el tipo de mensaje es puramente TEXT.
     */
    @Field("metadata")
    private MessageMetadataModel metadata;

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
    @Builder.Default
    @Field("sent_at")
    private Instant sentAt = Instant.now();

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

    /**
     * Clase que representa el sub-documento de metadatos en MongoDB.
     */
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class MessageMetadataModel {

        @Field("file_name")
        private String fileName;

        @Field("size_bytes")
        private Long sizeBytes;

        @Field("width")
        private Integer width;

        @Field("height")
        private Integer height;

        @Field("duration_seconds")
        private Integer durationSeconds;

        @Field("mime_type")
        private String mimeType;
    }
}