package com.frandm.healthtracker.backend.auth.service;

import com.frandm.healthtracker.backend.auth.config.AuthProperties;
import com.frandm.healthtracker.backend.auth.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final AuthProperties authProperties;
    private final SecretKey secretKey;
    private final Clock clock;

    public JwtTokenService(AuthProperties authProperties, Clock clock) {
        this.authProperties = authProperties;
        this.clock = clock;
        this.secretKey = Keys.hmacShaKeyFor(authProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UUID userId) {
        Instant now = clock.instant();
        Instant expiration = now.plusSeconds(authProperties.getJwt().getAccessTokenSeconds());
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    public UUID parseUserId(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
            return UUID.fromString(claims.getSubject());
        } catch (RuntimeException ex) {
            throw new InvalidTokenException("Access token is invalid.");
        }
    }

    public long getAccessTokenTtlSeconds() {
        return authProperties.getJwt().getAccessTokenSeconds();
    }
}
