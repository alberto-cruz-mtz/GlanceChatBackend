package alberto.cruz.mtz.glance.chat.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatResponse(
        String id,
        Recipient recipient,
        String lastMessage
) {
}
