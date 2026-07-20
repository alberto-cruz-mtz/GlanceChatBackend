package alberto.cruz.mtz.glance.chat.backend.service;

import alberto.cruz.mtz.glance.chat.backend.dto.CursorPage;
import alberto.cruz.mtz.glance.chat.backend.dto.DataResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.MessageMetadata;
import alberto.cruz.mtz.glance.chat.backend.dto.MessageRequest;
import alberto.cruz.mtz.glance.chat.backend.dto.MessageResponse;
import alberto.cruz.mtz.glance.chat.backend.model.Message;
import alberto.cruz.mtz.glance.chat.backend.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;

    @Override
    public String saveMessage(List<String> conversationIds, MessageRequest request) {
        Message.MessageMetadataModel metadata = Message.MessageMetadataModel.from(request.metadata());

        Message message = Message.builder()
                .conversationIds(conversationIds)
                .senderId(request.senderId())
                .content(request.content())
                .type(request.type())
                .metadata(metadata)
                .build();

        Message createdMessage = messageRepository.save(message);
        return createdMessage.getId();
    }

    @Override
    public DataResponse<MessageResponse> getMessagesByConversationId(String conversationId, Instant before, Pageable pageable) {
        // The incoming Pageable carries the client-requested size. We fetch one extra message
        // (a "probe") to detect if there are more messages without a separate count query.
        int requestedSize = pageable.getPageSize();
        Pageable probePageable = PageRequest.of(
                pageable.getPageNumber(),
                requestedSize + 1,
                pageable.getSort()
        );

        List<Message> fetched = (before == null)
                ? messageRepository.findByConversationIds(conversationId, probePageable)
                : messageRepository.findOlderByConversationIds(conversationId, before, probePageable);

        boolean hasNext = fetched.size() > requestedSize;
        List<Message> page = hasNext ? fetched.subList(0, requestedSize) : fetched;

        // nextCursor = sentAt of the oldest message in this page. Null when no more pages.
        String nextCursor = hasNext
                ? page.get(page.size() - 1).getSentAt().toString()
                : null;

        List<MessageResponse> messages = page.stream()
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

        return new DataResponse<>(messages, new CursorPage(nextCursor));
    }
}
