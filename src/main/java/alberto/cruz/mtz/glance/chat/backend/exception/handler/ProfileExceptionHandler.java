package alberto.cruz.mtz.glance.chat.backend.exception.handler;

import alberto.cruz.mtz.glance.chat.backend.exception.AvatarUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class ProfileExceptionHandler {

    @Value("${error.url}")
    private static String ERROR_URL;

    @ExceptionHandler(AvatarUploadException.class)
    public ResponseEntity<ProblemDetail> handleAvatarUploadException(AvatarUploadException exception) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        problemDetail.setTitle("Avatar Upload Failed");
        problemDetail.setType(URI.create(ERROR_URL + "/problems/avatar-upload-failed"));

        return ResponseEntity.status(status).body(problemDetail);
    }
}
