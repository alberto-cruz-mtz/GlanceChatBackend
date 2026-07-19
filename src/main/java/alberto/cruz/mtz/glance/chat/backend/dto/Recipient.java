package alberto.cruz.mtz.glance.chat.backend.dto;

public record Recipient(
        String id,
        String publicId,
        // el name puede ser el username o displayName dependiendo
        // si el usuario configuro un displayName o no, si no tiene displayName se mostrara el username
        String name,
        String avatar
) {
}
