package alberto.cruz.mtz.glance.chat.backend.dto;

public record MessageResponse(
        String id,
        String content,
        String chatId,
        String senderId
) {
}
