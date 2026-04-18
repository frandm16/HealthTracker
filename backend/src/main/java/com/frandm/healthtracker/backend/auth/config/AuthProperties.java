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

    //region Jwt

    public Jwt getJwt() {
        return jwt;
    }

    public static class Jwt {
        @NotBlank
        private String secret;

        @Min(1)
        private long accessTokenSeconds = 900; // 15min

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getAccessTokenSeconds() {
            return accessTokenSeconds;
        }

        public void setAccessTokenSeconds(long accessTokenSeconds) {
            this.accessTokenSeconds = accessTokenSeconds;
        }
    }

    //endregion
    //region Refresh Token

    public RefreshToken getRefreshToken() {
        return refreshToken;
    }

    public static class RefreshToken {
        @Min(1)
        private long refreshTokenSeconds = 2592000; // 72h

        public long getRefreshTokenSeconds() {
            return refreshTokenSeconds;
        }

        public void setRefreshTokenSeconds(long refreshTokenSeconds) {
            this.refreshTokenSeconds = refreshTokenSeconds;
        }
    }

    //endregion
    //region Google

    public Google getGoogle() {
        return google;
    }

    public static class Google {
        @NotBlank
        private String googleClientId;

        public String getGoogleClientId() {
            return googleClientId;
        }

        public void setGoogleClientId(String googleClientId) {
            this.googleClientId = googleClientId;
        }
    }
    //endregion
}
