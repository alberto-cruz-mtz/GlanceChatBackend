package alberto.cruz.mtz.glance.chat.backend.configuration;

import alberto.cruz.mtz.glance.chat.backend.exception.InvalidJwtException;
import alberto.cruz.mtz.glance.chat.backend.exception.UnknownException;
import alberto.cruz.mtz.glance.chat.backend.util.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public @NonNull Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        assert accessor != null;
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.info("Iniciando autenticacion para el websocket");
            String authorization = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
            try {
                if (authorization != null && authorization.startsWith("Bearer ")) {
                    String token = authorization.substring(7);
                    DecodedJWT decodedJWT = jwtUtil.verify(token);

                    String userId = jwtUtil.getUserId(decodedJWT);

                    Principal principal = new UserPrincipal(userId);
                    accessor.setUser(principal);

                    log.info("User authenticated: {}", principal);
                } else {
                    log.error("Authorization header is missing or invalid");
                    throw new UnknownException("Authorization header is missing or invalid");
                }
            } catch (InvalidJwtException invalidJwtException) {
                throw new MessageDeliveryException("Forbidden: Invalid access token, please provide a valid access token to connect to the WebSocket.");
            } catch (UnknownException unknownException) {
                throw new MessageDeliveryException("Forbidden: Authorization header is missing or invalid, please provide a valid access token to connect to the WebSocket.");
            }
        }

        return message;
    }
}
