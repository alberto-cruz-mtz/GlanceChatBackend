package alberto.cruz.mtz.glance.chat.backend.exception;

public class InvalidTotpCodeException extends RuntimeException {
    public InvalidTotpCodeException(String message) {
        super(message);
    }
}
