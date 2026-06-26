package alberto.cruz.mtz.glance.chat.backend.exception.handler;

import alberto.cruz.mtz.glance.chat.backend.exception.UsernameAlreadyInUseException;
import alberto.cruz.mtz.glance.chat.backend.exception.InvalidOtpException;
import alberto.cruz.mtz.glance.chat.backend.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class AuthenticationExceptionHandler {

    @Value("${error.url}")
    private static String ERROR_URL;

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleUserNotFound(UserNotFoundException exception) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        problemDetail.setTitle("User not found");
        problemDetail.setType(URI.create(ERROR_URL + "/user-not-found"));

        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(UsernameAlreadyInUseException.class)
    public ResponseEntity<ProblemDetail> handleEmailAlreadyInUseException(UsernameAlreadyInUseException exception) {
        HttpStatus status = HttpStatus.CONFLICT;

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        problemDetail.setTitle("Email Already In Use");
        problemDetail.setType(URI.create(ERROR_URL + "/authentication/email-already-in-use"));

        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ProblemDetail> handleInvalidOtp(InvalidOtpException exception) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        problemDetail.setTitle("Invalid OTP Code");
        problemDetail.setType(URI.create(ERROR_URL + "/authentication/invalid-otp-code"));

        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<ProblemDetail> handleAuthentication(AuthenticationException exception) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        String message = "Credentials incorrect, Please verify your credentials";

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);
        problemDetail.setType(URI.create(ERROR_URL + "/authentication"));
        problemDetail.setTitle("Authentication failed");

        return ResponseEntity.status(status).body(problemDetail);
    }
}
