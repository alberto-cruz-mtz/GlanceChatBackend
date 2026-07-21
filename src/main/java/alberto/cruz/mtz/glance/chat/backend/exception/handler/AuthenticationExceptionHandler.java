package alberto.cruz.mtz.glance.chat.backend.exception.handler;

import alberto.cruz.mtz.glance.chat.backend.exception.DeviceCodeHasAlreadyBeenUsedException;
import alberto.cruz.mtz.glance.chat.backend.exception.InvalidOrExpiredDeviceCodeException;
import alberto.cruz.mtz.glance.chat.backend.exception.InvalidTemporaryTokenException;
import alberto.cruz.mtz.glance.chat.backend.exception.InvalidTotpCodeException;
import alberto.cruz.mtz.glance.chat.backend.exception.TwoFactorAuthenticationNotActiveException;
import alberto.cruz.mtz.glance.chat.backend.exception.UsernameAlreadyInUseException;
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
    private String ERROR_URL;

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

    @ExceptionHandler(InvalidTotpCodeException.class)
    public ResponseEntity<ProblemDetail> handleInvalidTotp(InvalidTotpCodeException exception) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        problemDetail.setTitle("Invalid TOTP Code");
        problemDetail.setType(URI.create(ERROR_URL + "/authentication/invalid-totp-code"));

        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<ProblemDetail> handleAuthentication(AuthenticationException exception) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        String message = "Credentials incorrect, Please verify your credentials";

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);
        problemDetail.setType(URI.create(ERROR_URL + "/authentication"));
        problemDetail.setTitle("Authentication failed");

        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(InvalidOrExpiredDeviceCodeException.class)
    public ResponseEntity<ProblemDetail> handleInvalidOrExpiredDeviceCode(InvalidOrExpiredDeviceCodeException exception) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        problemDetail.setTitle("Invalid or Expired Device Code");
        problemDetail.setType(URI.create(ERROR_URL + "/authentication/invalid-or-expired-device-code"));

        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(DeviceCodeHasAlreadyBeenUsedException.class)
    public ResponseEntity<ProblemDetail> handleDeviceCodeHasAlreadyBeenUsed(DeviceCodeHasAlreadyBeenUsedException exception) {
        HttpStatus status = HttpStatus.CONFLICT;

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        problemDetail.setTitle("Device Code Already Used");
        problemDetail.setType(URI.create(ERROR_URL + "/authentication/device-code-already-used"));

        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(InvalidTemporaryTokenException.class)
    public ResponseEntity<ProblemDetail> handleInvalidTemporaryToken(InvalidTemporaryTokenException exception) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        problemDetail.setTitle("Invalid Temporary Token");
        problemDetail.setType(URI.create(ERROR_URL + "/authentication/invalid-temporary-token"));

        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(TwoFactorAuthenticationNotActiveException.class)
    public ResponseEntity<ProblemDetail> handleTwoFactorAuthenticationNotActiveException(TwoFactorAuthenticationNotActiveException exception) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        problemDetail.setTitle("Two-factor authentication is not active");
        problemDetail.setInstance(URI.create(ERROR_URL + "/authentication/two-factor-authentication-not-active"));

        return ResponseEntity.status(status).body(problemDetail);
    }
}
