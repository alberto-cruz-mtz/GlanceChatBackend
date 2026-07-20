package alberto.cruz.mtz.glance.chat.backend.service;

import alberto.cruz.mtz.glance.chat.backend.dto.DataResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.MessageRequest;
import alberto.cruz.mtz.glance.chat.backend.dto.MessageResponse;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface MessageService {

    /**
     * @return the ID of the saved message
     */
    String saveMessage(List<String> conversationIds, MessageRequest request);

    /**
     * Retrieves messages for a conversation using cursor-based pagination.
     *
     * @param conversationId the conversation (chat) identifier
     * @param before         cursor; when non-null, returns messages strictly older than this instant.
     *                       When null, returns the most recent messages.
     * @param pageable       pagination controls (page number is ignored — use {@code limit} on the controller)
     */
    DataResponse<MessageResponse> getMessagesByConversationId(String conversationId, Instant before, Pageable pageable);
}
