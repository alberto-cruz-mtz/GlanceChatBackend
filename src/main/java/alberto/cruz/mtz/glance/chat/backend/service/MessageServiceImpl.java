package alberto.cruz.mtz.glance.chat.backend.service;

import alberto.cruz.mtz.glance.chat.backend.dto.ContentType;
import alberto.cruz.mtz.glance.chat.backend.dto.DataResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.MessageMetadata;
import alberto.cruz.mtz.glance.chat.backend.dto.MessageRequest;
import alberto.cruz.mtz.glance.chat.backend.dto.PaginationLinks;
import alberto.cruz.mtz.glance.chat.backend.dto.MessageResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.Pagination;
import alberto.cruz.mtz.glance.chat.backend.model.Message;
import alberto.cruz.mtz.glance.chat.backend.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;

    @Override
    public String saveMessage(List<String> conversationIds, MessageRequest request) {
        Message.MessageMetadataModel metadata = Message.MessageMetadataModel.from(request.metadata()); // Convertir MessageMetadata a MessageMetadataModel

        Message message = Message.builder()
                .conversationIds(conversationIds)
                .senderId(request.senderId())
                .content(request.content())
                .metadata(metadata)
                .build();

        Message createdMessage = messageRepository.save(message);
        return createdMessage.getId();
    }

    @Override
    public DataResponse<MessageResponse> getMessagesByConversationId(Pageable pageable, String conversationId) {
        var messagePage = messageRepository.findByConversationIds(conversationId, pageable);

        int currentPage = messagePage.getNumber();
        int perPage = messagePage.getSize();

        String prev = messagePage.hasPrevious() ? "http://localhost:8080/api/messages?page=" + (currentPage - 1) + "&size=" + perPage : null;
        String next = messagePage.hasNext() ? "http://localhost:8080/api/messages?page=" + (currentPage + 1) + "&size=" + perPage : null;

        PaginationLinks links = new PaginationLinks(prev, next);

        Pagination paginationRequest = new Pagination(currentPage, perPage, messagePage.getNumberOfElements(), messagePage.hasNext(), links);

        List<MessageResponse> messages = messagePage
                .map(message -> {
                    var metadata = new MessageMetadata(
                            message.getMetadata() != null ? message.getMetadata().getFileName() : null,
                            message.getMetadata() != null ? message.getMetadata().getSizeBytes() : null,
                            message.getMetadata() != null ? message.getMetadata().getWidth() : null,
                            message.getMetadata() != null ? message.getMetadata().getHeight() : null,
                            message.getMetadata() != null ? message.getMetadata().getDurationSeconds() : null,
                            message.getMetadata() != null ? message.getMetadata().getMimeType() : null
                    );

                    return new MessageResponse(
                            message.getId(),
                            message.getContent(),
                            conversationId,
                            message.getSenderId(),
                            message.getSentAt(),
                            message.getType(),
                            metadata
                    );
                })
                .toList();

        return new DataResponse<>(messages, paginationRequest);
    }
}
