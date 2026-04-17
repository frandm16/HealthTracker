package com.frandm.healthtracker.backend.auth.service;

import com.frandm.healthtracker.backend.auth.config.AuthProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    private final AuthProperties authProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    public RefreshToken generate() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return new RefreshToken(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
    }

    public String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available.", ex);
        }
    }

    public long getTtlSeconds() {
        return authProperties.getRefreshToken().getTtlSeconds();
    }

    public record RefreshToken(String value) {
    }
}
