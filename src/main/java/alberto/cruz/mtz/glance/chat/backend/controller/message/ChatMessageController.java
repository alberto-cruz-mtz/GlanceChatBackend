package alberto.cruz.mtz.glance.chat.backend.controller.message;

import alberto.cruz.mtz.glance.chat.backend.dto.MessageRequest;
import alberto.cruz.mtz.glance.chat.backend.dto.MessageResponse;
import alberto.cruz.mtz.glance.chat.backend.exception.UnauthorizedAccessException;
import alberto.cruz.mtz.glance.chat.backend.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatService chatService;

    @MessageMapping("chat.message.send")
    public void sendMessage(Principal principal, @Payload @Valid MessageRequest messageRequest) {
        String senderId = principal.getName();
        String recipientId = messageRequest.recipientId();
        String content = messageRequest.content();

        if (!senderId.equals(messageRequest.senderId())) {
            throw new UnauthorizedAccessException("Sender ID does not match the authenticated user");
        }

        String chatIdOfSender = chatService.recordMessageAndCreateConversationIfNeeded(senderId, recipientId, content);
        String chatIdOfRecipient = chatService.recordMessageAndCreateConversationIfNeeded(recipientId, senderId, content);

        MessageResponse messageResponseForSender = new MessageResponse(content, chatIdOfSender, senderId);
        MessageResponse messageResponseForRecipient = new MessageResponse(content, chatIdOfRecipient, senderId);

        simpMessagingTemplate.convertAndSendToUser(senderId, "/queue/private.messages", messageResponseForSender);
        simpMessagingTemplate.convertAndSendToUser(recipientId, "/queue/private.messages", messageResponseForRecipient);
    }
}
