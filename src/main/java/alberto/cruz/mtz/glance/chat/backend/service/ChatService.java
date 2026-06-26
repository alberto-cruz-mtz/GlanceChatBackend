package alberto.cruz.mtz.glance.chat.backend.service;

import alberto.cruz.mtz.glance.chat.backend.dto.ChatResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.DataResponse;

public interface ChatService {

    ChatResponse registerNewChat(String userId, String recipientPublicId);

    DataResponse<ChatResponse> getAllChats(String userId);

    ChatResponse getChat(String userId, String chatId);
}
