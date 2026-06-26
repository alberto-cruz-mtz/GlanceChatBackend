package alberto.cruz.mtz.glance.chat.backend.dto;

public record MessageResponse(
        String content,
        String chatId,
        String senderId
) {
}
