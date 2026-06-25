package alberto.cruz.mtz.glance.chat.backend.exception;

public class TwoFactorAuthenticationNotActiveException extends RuntimeException {
    public TwoFactorAuthenticationNotActiveException(String message) {
        super(message);
    }
}
