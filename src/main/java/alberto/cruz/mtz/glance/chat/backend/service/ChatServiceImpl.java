package alberto.cruz.mtz.glance.chat.backend.service;

import alberto.cruz.mtz.glance.chat.backend.dto.ChatResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.DataResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.Recipient;
import alberto.cruz.mtz.glance.chat.backend.exception.UnknownException;
import alberto.cruz.mtz.glance.chat.backend.exception.UserNotFoundException;
import alberto.cruz.mtz.glance.chat.backend.model.Conversation;
import alberto.cruz.mtz.glance.chat.backend.model.LastMessage;
import alberto.cruz.mtz.glance.chat.backend.model.User;
import alberto.cruz.mtz.glance.chat.backend.repository.ConversationRepository;
import alberto.cruz.mtz.glance.chat.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    /**
     *
     */
    @Override
    public ChatResponse registerNewChat(String userId, String recipientPublicId) {
        User recipient = userRepository.findByPublicId(recipientPublicId)
                .orElseThrow(() -> new UnknownException("This public ID not exists, please check it again"));

        if (conversationRepository.existsBySenderIdAndRecipientId(userId, recipient.getId())) {
            throw new UnknownException("Conversation already exists");
        }

        Conversation conversation = new Conversation(null, userId, recipient.getId(), null, Instant.now());
        Conversation createdConversation = conversationRepository.save(conversation);

        String recipientName = recipient.getDisplayName() != null ? recipient.getDisplayName() : recipient.getUsername();
        Recipient recipientInfo = new Recipient(recipient.getId(), recipientName, recipient.getAvatarUrl());

        return new ChatResponse(createdConversation.getId(), recipientInfo);
    }

    @Override
    public DataResponse<ChatResponse> getAllChats(String userId) {
        var conversations = conversationRepository.findBySenderId(userId);
        var recipientsId = conversations.stream()
                .map(Conversation::getRecipientId)
                .toList();

        var recipients = userRepository.findAllById(recipientsId)
                .stream().collect(Collectors.toMap(User::getId, Function.identity()));

        List<ChatResponse> chats = conversations.stream()
                .map(conversation -> {
                    User recipient = recipients.get(conversation.getRecipientId());

                    String recipientName = recipient.getDisplayName() != null ? recipient.getDisplayName() : recipient.getUsername();
                    Recipient recipientInfo = new Recipient(recipient.getId(), recipientName, recipient.getAvatarUrl());
                    return new ChatResponse(conversation.getId(), recipientInfo);
                }).toList();

        return new DataResponse<>(chats);
    }

    /**
     * recuperar la conversación por el ID
     * validar que el chat pertenece al usuario que lo solicita
     * convertirlo a un ChatResponse
     */
    @Override
    public ChatResponse getChat(String userId, String chatId) {
        Conversation conversation = conversationRepository.findById(chatId)
                .orElseThrow(() -> new UnknownException("Conversation not found"));

        if (!conversation.getSenderId().equals(userId)) {
            throw new UnknownException("You are not allowed to access this conversation");
        }

        User recipient = userRepository.findById(conversation.getRecipientId())
                .orElseThrow(() -> new UnknownException("Recipient not found"));

        String recipientName = recipient.getDisplayName() != null ? recipient.getDisplayName() : recipient.getUsername();
        Recipient recipientInfo = new Recipient(recipient.getId(), recipientName, recipient.getAvatarUrl());

        return new ChatResponse(conversation.getId(), recipientInfo);
    }

}
