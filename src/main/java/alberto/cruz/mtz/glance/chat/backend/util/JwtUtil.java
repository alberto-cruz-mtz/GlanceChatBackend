package alberto.cruz.mtz.glance.chat.backend.util;

import alberto.cruz.mtz.glance.chat.backend.exception.InvalidJwtException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class JwtUtil {

    private final Long expiration;
    private final String issuer;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") Long expiration,
            @Value("${jwt.issuer}") String issuer
    ) {
        this.expiration = expiration;
        this.issuer = issuer;

        this.algorithm = Algorithm.HMAC256(secret);

        this.verifier = JWT.require(this.algorithm)
                .withIssuer(this.issuer)
                .build();
    }

    public String generateToken(String username, String userId, String sessionId) {
        return JWT.create()
                .withSubject(username)
                .withIssuer(this.issuer)
                .withExpiresAt(Instant.now().plusMillis(this.expiration))
                .withIssuedAt(Instant.now())
                .withNotBefore(Instant.now())
                .withClaim("id", userId)
                .withClaim("session_id", sessionId)
                .sign(this.algorithm);
    }

    public DecodedJWT verify(DecodedJWT token) {
        try {
            return this.verifier.verify(token.getToken());
        } catch (JWTVerificationException jwtVerificationException) {
            throw new InvalidJwtException("JWT Invalid: Token missing, malformed, expired or with an invalid signature");
        }
    }

    public String getClaim(DecodedJWT decodedJWT, String name) {
        return decodedJWT.getClaim(name).as(String.class);
    }

    public String getUserId(DecodedJWT decodedJWT) {
        return this.getClaim(decodedJWT, "id");
    }

    public String getSessionId(DecodedJWT decodedJWT) {
        return this.getClaim(decodedJWT, "session_id");
    }

    public String getUsername(DecodedJWT decodedJWT) {
        return decodedJWT.getSubject();
    }
}
