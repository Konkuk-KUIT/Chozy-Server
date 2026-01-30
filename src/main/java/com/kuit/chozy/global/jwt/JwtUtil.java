package com.kuit.chozy.global.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.time.Instant;
import java.util.Date;

@Component
@Getter
public class JwtUtil {
    private final SecretKey secretKey;

    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
            @Value("${jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generateAccessToken(Long userId) {
        return generateToken(userId, accessTokenExpirationMs);
    }

    public String generateRefreshToken(Long userId) {
        return generateToken(userId, refreshTokenExpirationMs);
    }

    public String generateToken(Long userId, Long expireMs) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(expireMs);

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(secretKey)
                .compact();
    }

    public Long getUserId(String token) {
        String sub = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

        return Long.parseLong(sub);
    }

    public long getAccessTokenExpiresInSeconds() {
        return accessTokenExpirationMs / 1000;
    }

    public long getRefreshTokenExpiresInSeconds() {
        return refreshTokenExpirationMs / 1000;
    }
}
