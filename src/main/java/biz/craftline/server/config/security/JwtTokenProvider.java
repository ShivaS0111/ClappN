package biz.craftline.server.config.security;

import biz.craftline.server.feature.usermanagement.api.controller.AuthController;
import biz.craftline.server.feature.usermanagement.domain.model.TokenInfo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:604800000}") // 7 days
    private long jwtExpirationInMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }



    // Helper methods
    private String generateRefreshToken(String username) {
        String refreshToken = UUID.randomUUID().toString();
        return refreshToken;
    }

    public TokenInfo generateTokenWithPermissions(String username, List<String> permissions) {
        return generateTokenWithClaims(username, permissions, List.of(), List.of(), List.of());
    }

    public TokenInfo generateTokenWithClaims(String username, List<String> permissions,
                                              List<String> roles, List<Long> storeIds, List<Long> businessIds) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);

        String jwt = Jwts.builder()
                .subject(username)
                .claim("permissions", permissions)
                .claim("roles", roles)
                .claim("storeIds", storeIds)
                .claim("businessIds", businessIds)
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();

        String refreshToken = generateRefreshToken(username);
        return TokenInfo.builder()
                .token(jwt)
                .tokenExpiry(expiryDate.toInstant().getEpochSecond())
                .refreshToken(refreshToken)
                .build();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getPermissionsFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return (List<String>) claims.get("permissions");
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        List<String> roles = (List<String>) claims.get("roles");
        return roles != null ? roles : List.of();
    }

    @SuppressWarnings("unchecked")
    public List<Long> getStoreIdsFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        List<?> raw = (List<?>) claims.get("storeIds");
        return raw != null ? raw.stream().map(v -> ((Number) v).longValue()).toList() : List.of();
    }

    @SuppressWarnings("unchecked")
    public List<Long> getBusinessIdsFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        List<?> raw = (List<?>) claims.get("businessIds");
        return raw != null ? raw.stream().map(v -> ((Number) v).longValue()).toList() : List.of();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }
}
