package alberto.cruz.mtz.glance.chat.backend.exception;

public class DeviceCodeHasAlreadyBeenUsedException extends RuntimeException {
    public DeviceCodeHasAlreadyBeenUsedException(String message) {
        super(message);
    }
}
