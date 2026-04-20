package com.frandm.healthtracker.backend.auth.controller;

import com.frandm.healthtracker.backend.auth.dto.AuthDtos.AuthResponse;
import com.frandm.healthtracker.backend.auth.dto.AuthDtos.GoogleSignInRequest;
import com.frandm.healthtracker.backend.auth.dto.AuthDtos.RefreshTokenRequest;
import com.frandm.healthtracker.backend.auth.dto.AuthDtos.UserResponse;
import com.frandm.healthtracker.backend.auth.service.AuthService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/google/sign-in")
    public ResponseEntity<AuthResponse> signInWithGoogle(@Valid @RequestBody GoogleSignInRequest request) {
        return ResponseEntity.ok(authService.signInWithGoogle(request.idToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(authService.getCurrentUser(userId));
    }
}
