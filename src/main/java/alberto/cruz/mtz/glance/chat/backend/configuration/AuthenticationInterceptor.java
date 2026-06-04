package alberto.cruz.mtz.glance.chat.backend.configuration;

import com.sun.security.auth.UserPrincipal;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;

@Component
public class AuthenticationInterceptor implements ChannelInterceptor {

    @Override
    public @NonNull Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        assert accessor != null;
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorization = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);

            // TODO: cambiar el valor de recuperación de la cabecera de UUID a un Token
            if (authorization != null && !authorization.isEmpty()) {
                UUID uid = UUID.fromString(authorization);

                Principal principal = new UserPrincipal(uid.toString());
                accessor.setUser(principal);
            }
        }

        return message;
    }
}
