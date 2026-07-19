package alberto.cruz.mtz.glance.chat.backend.dto;

import java.time.Instant;

public record MessageResponse(
        String id,
        String content,
        String chatId,
        String senderId,
        Instant sendAt,
        ContentType type,
        MessageMetadata metadata // Nuevo campo para alojar los datos del archivo
) {
}