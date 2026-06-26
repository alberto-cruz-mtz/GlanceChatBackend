package alberto.cruz.mtz.glance.chat.backend.exception.handler;

import alberto.cruz.mtz.glance.chat.backend.exception.ConversationAlreadyExistsException;
import alberto.cruz.mtz.glance.chat.backend.exception.ConversationNotFoundException;
import alberto.cruz.mtz.glance.chat.backend.exception.InvalidPublicIdException;
import alberto.cruz.mtz.glance.chat.backend.exception.UnauthorizedAccessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class ChatExceptionHandler {

    @Value("${error.url}")
    private static String ERROR_URL;

    @ExceptionHandler({InvalidPublicIdException.class})
    public ResponseEntity<ProblemDetail> handleInvalidPublicId(InvalidPublicIdException exception) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        problemDetail.setTitle("Invalid Public ID");
        problemDetail.setType(URI.create(ERROR_URL + "/invalid-public-id"));

        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler({ConversationAlreadyExistsException.class})
    public ResponseEntity<ProblemDetail> handleConversationAlreadyExists(ConversationAlreadyExistsException exception) {
        HttpStatus status = HttpStatus.CONFLICT;

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        problemDetail.setTitle("Conversation Already Exists");
        problemDetail.setType(URI.create(ERROR_URL + "/conversation-already-exists"));

        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler({ConversationNotFoundException.class})
    public ResponseEntity<ProblemDetail> handleConversationNotFound(ConversationNotFoundException exception) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        problemDetail.setTitle("Conversation Not Found");
        problemDetail.setType(URI.create(ERROR_URL + "/conversation-not-found"));

        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler({UnauthorizedAccessException.class})
    public ResponseEntity<ProblemDetail> handleUnauthorizedAccess(UnauthorizedAccessException exception) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        problemDetail.setTitle("Unauthorized Access");
        problemDetail.setType(URI.create(ERROR_URL + "/unauthorized-access"));

        return ResponseEntity.status(status).body(problemDetail);
    }
}
