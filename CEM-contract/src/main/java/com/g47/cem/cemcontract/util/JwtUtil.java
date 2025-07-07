package com.g47.cem.cemcontract.util;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Utility class for JWT token operations
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        JwtParser parser = Jwts.parser()
                .verifyWith(getSigningKey())
                .build();
        
        return parser.parseSignedClaims(token).getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }

    public List<SimpleGrantedAuthority> extractAuthorities(String token) {
        Claims claims = extractAllClaims(token);
        List<String> roles = claims.get("roles", List.class);
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        List<String> roles = claims.get("roles", List.class);
        if (roles == null || roles.isEmpty()) {
            return claims.get("role", String.class); // Fallback
        }
        return roles.get(0);
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object userIdObj = claims.get("userId");
        
        if (userIdObj == null) {
            return null;
        }
        
        // Handle different number types that might be in the JWT
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        } else if (userIdObj instanceof String) {
            try {
                return Long.parseLong((String) userIdObj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        return null;
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public Long getExpirationTime() {
        return expiration;
    }
    
    /**
     * Get user ID from token - alias for extractUserId for compatibility
     */
    public Long getUserIdFromToken(String token) {
        return extractUserId(token);
    }
} 