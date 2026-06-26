package alberto.cruz.mtz.glance.chat.backend.exception;

public class ConversationAlreadyExistsException extends RuntimeException {
    public ConversationAlreadyExistsException(String message) {
        super(message);
    }
}

