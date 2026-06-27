package alberto.cruz.mtz.glance.chat.backend.exception;

public class InvalidOrExpiredDeviceCodeException extends RuntimeException {
    public InvalidOrExpiredDeviceCodeException(String message) {
        super(message);
    }
}
