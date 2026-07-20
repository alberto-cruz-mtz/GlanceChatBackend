package alberto.cruz.mtz.glance.chat.backend.controller.rest;

import alberto.cruz.mtz.glance.chat.backend.dto.ChatRequest;
import alberto.cruz.mtz.glance.chat.backend.dto.ChatResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.CursorPage;
import alberto.cruz.mtz.glance.chat.backend.dto.DataResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.MessageResponse;
import alberto.cruz.mtz.glance.chat.backend.service.ChatService;
import alberto.cruz.mtz.glance.chat.backend.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Objects;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Validated
@Tag(name = "Chat", description = "Chat and conversation management")
public class ChatController {

    private final ChatService chatService;
    private final MessageService messageService;

    @Operation(
            summary = "Create a new chat",
            description = "Creates a new conversation between the authenticated user and a recipient identified by their public ID.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Recipient public ID to start a conversation with",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ChatRequest.class),
                            examples = @ExampleObject(
                                    name = "Example Request",
                                    summary = "An example request for creating a new chat",
                                    value = """
                                            {
                                              "recipientPublicId": "A1B2C3D4"
                                            }
                                            """
                            )
                    )
            ),
            parameters = {
                    @Parameter(description = "Authenticated user", hidden = true)
            },
            tags = {"Chat", "Requires Authentication"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Chat created successfully",
                    content = @Content(
                            schema = @Schema(implementation = ChatResponse.class),
                            examples = @ExampleObject(
                                    name = "Example Response",
                                    summary = "An example response for creating a new chat",
                                    value = """
                                            {
                                              "id": "6a4455d22086f25f7da4dee7",
                                              "recipient": {
                                                "id": "6a40a03ca5ae73b226a4e71f",
                                                "name": "testuser12345",
                                                "avatar": null
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed or invalid public ID",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Example Response",
                                            summary = "An example response for a validation error when trying to create a chat with an invalid or empty public ID",
                                            value = """
                                                    {
                                                      "detail": "Recipient public ID cannot be empty",
                                                      "instance": "/api/chats",
                                                      "status": 400,
                                                      "title": "Validation Failed",
                                                      "type": "http://localhost:8080/error/validation-failed",
                                                      "fieldErrors": {
                                                        "recipientPublicId": "Recipient public ID cannot be empty"
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Example Response",
                                            summary = "An example response for an invalid public ID when trying to create a chat",
                                            value = """
                                                    {
                                                      "detail": "This public ID not exists, please check it again",
                                                      "instance": "/api/chats",
                                                      "status": 400,
                                                      "title": "Invalid Public ID",
                                                      "type": "null/invalid-public-id"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conversation already exists with this user",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(
                                    name = "Example Response",
                                    summary = "An example response for a conflict when trying to create a chat with an existing conversation",
                                    value = """
                                            {
                                              "detail": "You already have a conversation with this user",
                                              "instance": "/api/chats",
                                              "status": 409,
                                              "title": "Conversation Already Exists",
                                              "type": "http://localhost:8080/conversation-already-exists"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<ChatResponse> registerNewChat(
            Authentication authentication,
            @RequestBody @Valid ChatRequest request) {
        String userId = Objects.requireNonNull(authentication.getCredentials()).toString();
        var response = chatService.registerNewChat(userId, request.recipientPublicId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get all chats",
            description = "Retrieves all conversations for the authenticated user.",
            method = "GET",
            parameters = {
                    @Parameter(name = "authentication", description = "Authenticated user", hidden = true)
            },
            tags = {"Chat", "Requires Authentication"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Chats retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = DataResponse.class),
                            examples = @ExampleObject(
                                    name = "Example Response",
                                    summary = "An example response for retrieving all chats",
                                    value = """
                                            {
                                              "data": [
                                                {
                                                  "id": "6a3e14f4e1142b630bb966b7",
                                                  "recipient": {
                                                    "id": "6a2cac017b3f2ebf959d3c53",
                                                    "name": "alberto812004@",
                                                    "avatar": "http://localhost:9000/avatars/6a2cac017b3f2ebf959d3c53/d4e79f6a-782a-464a-903c-197bc0c5504b.jpeg"
                                                  }
                                                },
                                                {
                                                  "id": "6a3e157559cddb4b8398f622",
                                                  "recipient": {
                                                    "id": "6a2cabbf7b3f2ebf959d3c52",
                                                    "name": "albertocruz",
                                                    "avatar": "http://localhost:9000/avatars/6a2cabbf7b3f2ebf959d3c52/d28ed32e-f08a-4bfe-a813-9819807e9905.jpeg"
                                                  }
                                                },
                                                {
                                                  "id": "6a40b396a5ae73b226a4e721",
                                                  "recipient": {
                                                    "id": "6a40a120a5ae73b226a4e720",
                                                    "name": "testuser12245",
                                                    "avatar": null
                                                  }
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping
    public ResponseEntity<DataResponse<ChatResponse>> getAllChats(Authentication authentication) {
        String userId = Objects.requireNonNull(authentication.getCredentials()).toString();
        var response = chatService.getAllChats(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get a chat by ID",
            description = "Retrieves a specific conversation by its ID. Only the owner of the conversation can access it.",
            method = "GET",
            parameters = {
                    @Parameter(name = "authentication", description = "Authenticated user", hidden = true),
                    @Parameter(name = "chatId", description = "Unique identifier of the conversation", required = true)
            },
            tags = {"Chat", "Requires Authentication"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Chat retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = ChatResponse.class),
                            examples = @ExampleObject(
                                    name = "Example Response",
                                    summary = "An example response for retrieving a specific chat by ID",
                                    value = """
                                            {
                                              "id": "6a3e157559cddb4b8398f622",
                                              "recipient": {
                                                "id": "6a2cabbf7b3f2ebf959d3c52",
                                                "name": "albertocruz",
                                                "avatar": "http://localhost:9000/avatars/6a2cabbf7b3f2ebf959d3c52/d28ed32e-f08a-4bfe-a813-9819807e9905.jpeg"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized access to this conversation",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "detail": "You are not allowed to access this conversation",
                                              "instance": "/api/chats/6a3ec718e70da8cc2283c19f",
                                              "status": 401,
                                              "title": "Unauthorized Access",
                                              "type": "http://localhost:8080/unauthorized-access"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conversation or recipient not found",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(
                                    name = "Example Response",
                                    summary = "An example response for a not found error when trying to retrieve a chat with an invalid or non-existent chat ID",
                                    value = """
                                            {
                                              "detail": "Conversation not found, please check the chat ID",
                                              "instance": "/api/chats/6a3ec718e70da8c83c19f",
                                              "status": 404,
                                              "title": "Conversation Not Found",
                                              "type": "http://localhost:8080/conversation-not-found"
                                            }
                                            """
                            )
                    ))
    })
    @GetMapping("/{chatId}")
    public ResponseEntity<ChatResponse> getChat(Authentication authentication, @PathVariable String chatId) {
        String userId = Objects.requireNonNull(authentication.getCredentials()).toString();
        var response = chatService.getChat(userId, chatId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get messages by chat ID",
            description = """
                    Retrieves messages for a specific conversation using cursor-based pagination.

                    - First call: omit `before` to get the most recent messages.
                    - Subsequent calls (e.g. on scroll-up): pass the `nextCursor` returned by the previous
                      response as `before` to fetch strictly older messages.
                    - The response includes `metadata.nextCursor`; when it is `null`, there are no more
                      older messages to load.
                    """,
            method = "GET",
            parameters = {
                    @Parameter(name = "chatId", description = "Unique identifier of the conversation", required = true),
                    @Parameter(name = "before", description = "ISO-8601 instant. Returns messages strictly older than this value. Omit for the first page."),
                    @Parameter(name = "limit", description = "Number of messages to return. Between 1 and 100. Defaults to 50.")
            },
            tags = {"Chat", "Messages", "Requires Authentication"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Messages retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = DataResponse.class),
                            examples = @ExampleObject(
                                    name = "Example Response",
                                    summary = "An example response with a nextCursor",
                                    value = """
                                            {
                                              "data": [
                                                {
                                                  "id": "6a4455d22086f25f7da4dee7",
                                                  "content": "Hola!",
                                                  "chatId": "6a3e157559cddb4b8398f622",
                                                  "senderId": "6a2cabbf7b3f2ebf959d3c52",
                                                  "sendAt": "2026-07-20T10:30:00Z",
                                                  "type": "TEXT",
                                                  "metadata": null
                                                }
                                              ],
                                              "metadata": {
                                                "nextCursor": "2026-07-20T10:30:00Z"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid limit or malformed `before` cursor",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping("/{chatId}/message")
    public ResponseEntity<DataResponse<MessageResponse>> getMessageByChat(
            @PathVariable String chatId,
            @RequestParam(required = false) Instant before,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit
    ) {
        // Server-side enforced sort — clients cannot override it.
        // The service is responsible for the +1 probe; we pass the requested size as-is.
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "sentAt"));

        var response = messageService.getMessagesByConversationId(chatId, before, pageable);
        return ResponseEntity.ok(response);
    }


}