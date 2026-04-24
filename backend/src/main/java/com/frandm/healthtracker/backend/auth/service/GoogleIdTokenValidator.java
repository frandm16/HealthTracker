package com.frandm.healthtracker.backend.auth.service;

import com.frandm.healthtracker.backend.auth.config.AuthProperties;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
public class GoogleIdTokenValidator {

    private static final String GOOGLE_ISSUER = "https://accounts.google.com";

    private final AuthProperties authProperties;
    private final JwtDecoder jwtDecoder;

    public GoogleIdTokenValidator(AuthProperties authProperties) {
        this.authProperties = authProperties;
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withIssuerLocation(GOOGLE_ISSUER).build();

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(GOOGLE_ISSUER);
        OAuth2TokenValidator<Jwt> withAudience = token -> {
            List<String> audience = token.getAudience();

            if (audience != null && audience.contains(authProperties.getGoogle().getGoogleClientId())) {
                return OAuth2TokenValidatorResult.success();
            }

            return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Google idToken audience is invalid.", null));
        };

        decoder.setJwtValidator(jwt -> {
            OAuth2TokenValidatorResult issuerResult = withIssuer.validate(jwt);
            if (issuerResult.hasErrors()) {
                return issuerResult;
            }
            return withAudience.validate(jwt);
        });
        this.jwtDecoder = decoder;
    }

    public GoogleUserInfo validate(String idToken) {
        try {
            Jwt jwt = jwtDecoder.decode(idToken);

            String subject = jwt.getSubject();
            String email = jwt.getClaimAsString("email");
            String name = jwt.getClaimAsString("name");

            if (subject == null || email == null || name == null || name.isBlank()) {
                throw new ResponseStatusException(UNAUTHORIZED, "Google idToken payload is incomplete.");
            }

            return new GoogleUserInfo(subject, email, name);
        } catch (JwtException ex) {
            throw new ResponseStatusException(UNAUTHORIZED, "Google idToken verification failed.");
        }
    }

    public record GoogleUserInfo(
            String subject,
            String email,
            String name
    ) {
    }
}
