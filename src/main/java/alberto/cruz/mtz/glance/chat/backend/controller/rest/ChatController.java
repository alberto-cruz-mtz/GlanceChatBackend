package alberto.cruz.mtz.glance.chat.backend.controller.rest;

import alberto.cruz.mtz.glance.chat.backend.dto.ChatRequest;
import alberto.cruz.mtz.glance.chat.backend.dto.ChatResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.DataResponse;
import alberto.cruz.mtz.glance.chat.backend.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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
@Tag(name = "Chat", description = "Chat and conversation management")
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "Create a new chat", description = "Creates a new conversation between the authenticated user and a recipient identified by their public ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Chat created successfully",
                    content = @Content(schema = @Schema(implementation = ChatResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed or invalid public ID",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conversation already exists with this user",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<ChatResponse> registerNewChat(
            @Parameter(description = "Authenticated user", hidden = true)
            Authentication authentication,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Recipient public ID to start a conversation with",
                    required = true)
            @RequestBody @Valid ChatRequest request) {
        String userId = Objects.requireNonNull(authentication.getCredentials()).toString();
        var response = chatService.registerNewChat(userId, request.recipientPublicId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get all chats", description = "Retrieves all conversations for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chats retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DataResponse.class)))
    })
    @GetMapping
    public ResponseEntity<DataResponse<ChatResponse>> getAllChats(
            @Parameter(description = "Authenticated user", hidden = true)
            Authentication authentication) {
        String userId = Objects.requireNonNull(authentication.getCredentials()).toString();
        var response = chatService.getAllChats(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get a chat by ID", description = "Retrieves a specific conversation by its ID. Only the owner of the conversation can access it.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chat retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ChatResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized access to this conversation",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Conversation or recipient not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{chatId}")
    public ResponseEntity<ChatResponse> getChat(
            @Parameter(description = "Authenticated user", hidden = true)
            Authentication authentication,
            @Parameter(description = "Unique identifier of the conversation", required = true)
            @PathVariable String chatId) {
        String userId = Objects.requireNonNull(authentication.getCredentials()).toString();
        var response = chatService.getChat(userId, chatId);
        return ResponseEntity.ok(response);
    }
}
