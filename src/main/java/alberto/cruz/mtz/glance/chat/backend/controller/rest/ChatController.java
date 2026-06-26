package alberto.cruz.mtz.glance.chat.backend.controller.rest;

import alberto.cruz.mtz.glance.chat.backend.dto.ChatRequest;
import alberto.cruz.mtz.glance.chat.backend.dto.ChatResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.DataResponse;
import alberto.cruz.mtz.glance.chat.backend.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponse> registerNewChat(Authentication authentication, @RequestBody @Valid ChatRequest request) {
        String userId = Objects.requireNonNull(authentication.getCredentials()).toString();
        var response = chatService.registerNewChat(userId, request.recipientPublicId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<DataResponse<ChatResponse>> getAllChats(Authentication authentication) {
        String userId = Objects.requireNonNull(authentication.getCredentials()).toString();
        var response = chatService.getAllChats(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<ChatResponse> getChat(Authentication authentication, @PathVariable String chatId) {
        String userId = Objects.requireNonNull(authentication.getCredentials()).toString();
        var response = chatService.getChat(userId, chatId);
        return ResponseEntity.ok(response);
    }
}
