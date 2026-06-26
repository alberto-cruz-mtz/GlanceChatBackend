package alberto.cruz.mtz.glance.chat.backend.exception.handler;

import alberto.cruz.mtz.glance.chat.backend.exception.UnauthorizedAccessException;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalMessageExceptionHandler {

    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemMessageDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (existing, replacement) -> existing));

        String firstErrorMessage = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("validation failed for the request parameters");

        return new ProblemMessageDetail(
                "VALIDATION_FAILED",
                firstErrorMessage,
                Map.of("fieldErrors", fieldErrors)
        );
    }

    @MessageExceptionHandler(UnauthorizedAccessException.class)
    public ProblemMessageDetail handleUnauthorizedAccessException(UnauthorizedAccessException exception) {
        String typeError = "UNAUTHORIZED_ACCESS";
        String message = exception.getMessage();

        return new ProblemMessageDetail(typeError, message, null);
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
record ProblemMessageDetail(
        String type,
        String detail,
        Map<String, Object> properties
) {
}