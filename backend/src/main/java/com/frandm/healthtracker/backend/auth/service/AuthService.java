package com.frandm.healthtracker.backend.auth.service;

import com.frandm.healthtracker.backend.auth.dto.AuthDtos.AuthResponse;
import com.frandm.healthtracker.backend.auth.dto.AuthDtos.UserResponse;
import com.frandm.healthtracker.backend.auth.model.AuthIdentityEntity;
import com.frandm.healthtracker.backend.auth.model.AuthSessionEntity;
import com.frandm.healthtracker.backend.auth.model.UserEntity;
import com.frandm.healthtracker.backend.auth.repository.AuthIdentityRepository;
import com.frandm.healthtracker.backend.auth.repository.AuthSessionRepository;
import com.frandm.healthtracker.backend.auth.repository.UserRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthService {

    private static final String GOOGLE_PROVIDER = "google";

    private final GoogleIdTokenValidator googleIdTokenValidator;
    private final UserRepository userRepository;
    private final AuthIdentityRepository authIdentityRepository;
    private final AuthSessionRepository authSessionRepository;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final Clock clock;

    public AuthService(
            GoogleIdTokenValidator googleIdTokenValidator,
            UserRepository userRepository,
            AuthIdentityRepository authIdentityRepository,
            AuthSessionRepository authSessionRepository,
            JwtTokenService jwtTokenService,
            RefreshTokenService refreshTokenService,
            Clock clock
    ) {
        this.googleIdTokenValidator = googleIdTokenValidator;
        this.userRepository = userRepository;
        this.authIdentityRepository = authIdentityRepository;
        this.authSessionRepository = authSessionRepository;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenService = refreshTokenService;
        this.clock = clock;
    }

    @Transactional
    public AuthResponse signInWithGoogle(String idToken) {
        GoogleIdTokenValidator.GoogleUserInfo googleUser = googleIdTokenValidator.validate(idToken);

        UserEntity user = authIdentityRepository.findByProviderAndProviderUserId(GOOGLE_PROVIDER, googleUser.subject())
                .map(AuthIdentityEntity::getUser)
                .orElseGet(() -> createUserWithIdentity(googleUser)); // if User doesn't exist, it creates a new User

        return createAuthResponse(user, OffsetDateTime.now(clock));
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        OffsetDateTime now = OffsetDateTime.now(clock);

        AuthSessionEntity session = getActiveSession(refreshToken, now);
        revokeSession(session, now); // Revokes old session

        return createAuthResponse(session.getUser(), now); // Creates new session
    }

    @Transactional
    public void logout(String refreshToken) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        authSessionRepository.findByRefreshTokenHash(refreshTokenService.hash(refreshToken))
                .filter(session -> !session.isRevoked())
                .ifPresent(session -> revokeSession(session, now)); // revokes Session if active
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Not found."));
        return toUserResponse(user);
    }

    private UserEntity createUserWithIdentity(GoogleIdTokenValidator.GoogleUserInfo googleUser) {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setEmail(googleUser.email());
        user.setName(googleUser.name());
        UserEntity savedUser = userRepository.save(user); // Saves the User

        AuthIdentityEntity identity = new AuthIdentityEntity();
        identity.setId(UUID.randomUUID());
        identity.setUser(savedUser);
        identity.setProvider(GOOGLE_PROVIDER);
        identity.setProviderUserId(googleUser.subject());
        authIdentityRepository.save(identity); // links Google to the User

        return savedUser;
    }

    private AuthSessionEntity getActiveSession(String refreshToken, OffsetDateTime now) {
        AuthSessionEntity session = authSessionRepository.findByRefreshTokenHash(refreshTokenService.hash(refreshToken))
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Unauthorized."));

        if (session.isRevoked() || session.isExpired(now)) { // expired Session
            throw new ResponseStatusException(UNAUTHORIZED, "Unauthorized.");
        }

        return session;
    }

    private void revokeSession(AuthSessionEntity session, OffsetDateTime now) {
        session.setRevokedAt(now);
        authSessionRepository.save(session);
    }

    private AuthResponse createAuthResponse(UserEntity user, OffsetDateTime now) {
        String accessToken = jwtTokenService.generateAccessToken(user.getId());
        RefreshTokenService.RefreshToken refreshToken = refreshTokenService.generate();

        AuthSessionEntity session = new AuthSessionEntity();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setRefreshTokenHash(refreshTokenService.hash(refreshToken.value()));
        session.setExpiresAt(now.plusSeconds(refreshTokenService.getTtlSeconds()));
        authSessionRepository.save(session);

        return new AuthResponse(
                accessToken,
                refreshToken.value(),
                jwtTokenService.getAccessTokenTtlSeconds(),
                toUserResponse(user)
        );
    }

    private UserResponse toUserResponse(UserEntity user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName());
    }
}
