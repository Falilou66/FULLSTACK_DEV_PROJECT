package sn.samabank.transaction.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import sn.samabank.transaction.config.SamaBankProperties;

import javax.crypto.SecretKey;
import java.util.UUID;

@Service
public class JwtService {

    private final SamaBankProperties properties;

    public JwtService(SamaBankProperties properties) {
        this.properties = properties;
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("[JWT] Token expired");
        } catch (JwtException e) {
            System.out.println("[JWT] Invalid token: " + e.getMessage());
        }
        return false;
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public String extractUsername(String token) {
        return parseClaims(token).get("username", String.class);
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(properties.getJwt().getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
