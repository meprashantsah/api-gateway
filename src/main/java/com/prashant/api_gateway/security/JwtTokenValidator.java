package com.prashant.api_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

@Slf4j
@Component
public class JwtTokenValidator {

    @Value("${jwt.public-key}")
    private String publicKeyString;

    @Value("${jwt.issuer:auth-service}")
    private String expectedIssuer;

    @Value("${jwt.audience:api-gateway}")
    private String expectedAudience;

    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        String cleaned = publicKeyString
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\s", "");
        if (cleaned.isBlank()) {
            throw new IllegalStateException("JWT public key is not configured");
        }
        byte[] decoded = Base64.getDecoder().decode(cleaned);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.publicKey = keyFactory.generatePublic(spec);
        log.info("JWT Public Key loaded successfully");
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .requireIssuer(expectedIssuer)
                    .requireAudience(expectedAudience)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException _) {
            log.warn("JWT token expired");
            throw new JwtValidationException("Token has expired");
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            throw new JwtValidationException("Invalid token: " + e.getMessage());
        }
    }

    public String getUserId(Claims claims) {
        return claims.getSubject();
    }

    public String getUsername(Claims claims) {
        return claims.get("username", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(Claims claims) {
        return claims.get("roles", List.class);
    }

    public static class JwtValidationException extends RuntimeException {
        public JwtValidationException(String message) {
            super(message);
        }
    }
}

