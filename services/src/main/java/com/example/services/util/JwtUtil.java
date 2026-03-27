package com.example.services.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpiration;


    public String generateToken(UUID userId, String email) {
        return generateToken(userId, email, accessTokenExpiration, "ACCESS");
    }

    public String generateRefreshToken(UUID userId, String email) {
        return generateToken(userId, email, refreshTokenExpiration, "REFRESH");
    }

    private String generateToken(UUID userId, String email, long accessTokenExpiration, String tokenType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("accessTokenExpiration", accessTokenExpiration);
        claims.put("tokenType", tokenType);

        return createToken(claims, userId, accessTokenExpiration);

    }

    private String createToken(Map<String, Object> claims, UUID userId, long expiration) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());

        return Jwts.builder().claims(claims)
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
            Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Claims extractClaims(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
            return Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (Exception e) {
            log.error("Failed validation failed: {}", e.getMessage());
            return null;
        }

    }

    public UUID extractUserId(String token) {
        Claims claims = extractClaims(token);
        return claims != null ? UUID.fromString(claims.getSubject()) : null;
    }

    public String extractEmail(String token) {
        Claims claims = extractClaims(token);
        return claims != null ? (String) claims.get("email") : null;
    }

    public boolean isTokenExpired(String token) {
        Claims claims = extractClaims(token);

        return claims != null && claims.getExpiration().before(new Date());
    }

    public String getTokenType(String token) {
        Claims claims = extractClaims(token);
        return claims != null ? (String) claims.get("tokenType") : null;
    }

}
