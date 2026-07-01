package alberto.cruz.mtz.glance.chat.backend.exception.handler;

import alberto.cruz.mtz.glance.chat.backend.exception.UnknownException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${error.url}")
    private String ERROR_URL;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationExceptions(MethodArgumentNotValidException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST; // 400

        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (existing, replacement) -> existing));

        String firstErrorMessage = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("validation failed for the request parameters");

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, firstErrorMessage);
        problemDetail.setTitle("Validation Failed");
        problemDetail.setProperty("fieldErrors", fieldErrors);
        problemDetail.setType(URI.create(ERROR_URL + "/validation-failed"));

        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(UnknownException.class)
    public ResponseEntity<ProblemDetail> handleUnknownException(UnknownException ignore) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, "An unexpected error occurred");
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create(ERROR_URL + "/internal-server-error"));

        log.warn("An unexpected error occurred: {}", ignore.getMessage());

        return ResponseEntity.status(status).body(problemDetail);
    }
}
