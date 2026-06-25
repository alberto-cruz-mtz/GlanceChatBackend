package alberto.cruz.mtz.glance.chat.backend.exception;

public class MissingAuthorizationHeaderException extends RuntimeException {
    public MissingAuthorizationHeaderException(String message) {
        super(message);
    }
}
