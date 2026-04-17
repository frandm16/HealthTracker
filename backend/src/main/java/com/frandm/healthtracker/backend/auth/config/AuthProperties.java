package com.frandm.healthtracker.backend.auth.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    private final Jwt jwt = new Jwt();
    private final RefreshToken refreshToken = new RefreshToken();
    private final Google google = new Google();

    public Jwt getJwt() {
        return jwt;
    }

    public RefreshToken getRefreshToken() {
        return refreshToken;
    }

    public Google getGoogle() {
        return google;
    }

    public static class Jwt {
        @NotBlank
        private String secret;

        @Min(1)
        private long accessTokenTtlSeconds = 900;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getAccessTokenTtlSeconds() {
            return accessTokenTtlSeconds;
        }

        public void setAccessTokenTtlSeconds(long accessTokenTtlSeconds) {
            this.accessTokenTtlSeconds = accessTokenTtlSeconds;
        }
    }

    public static class RefreshToken {
        @Min(1)
        private long ttlSeconds = 2592000;

        public long getTtlSeconds() {
            return ttlSeconds;
        }

        public void setTtlSeconds(long ttlSeconds) {
            this.ttlSeconds = ttlSeconds;
        }
    }

    public static class Google {
        @NotBlank
        private String audience;

        public String getAudience() {
            return audience;
        }

        public void setAudience(String audience) {
            this.audience = audience;
        }
    }
}
