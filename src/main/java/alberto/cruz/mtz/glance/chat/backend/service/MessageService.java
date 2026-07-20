package alberto.cruz.mtz.glance.chat.backend.service;

import alberto.cruz.mtz.glance.chat.backend.dto.DataResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.MessageRequest;
import alberto.cruz.mtz.glance.chat.backend.dto.MessageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MessageService {

    /**
     * @return the ID of the saved message
     */
    String saveMessage(List<String> conversationIds, MessageRequest request);

    DataResponse<MessageResponse> getMessagesByConversationId(Pageable pageable, String conversationId);
}
