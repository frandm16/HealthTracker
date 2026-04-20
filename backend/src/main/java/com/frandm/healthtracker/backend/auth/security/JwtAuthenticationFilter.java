package com.frandm.healthtracker.backend.auth.security;

import com.frandm.healthtracker.backend.auth.service.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);
            UUID userId = jwtTokenService.parseUserId(token); // userID from the token

            SecurityContextHolder.getContext().setAuthentication(new AuthenticatedUser(userId)); // Stores the user in authenticated Users
        }
        filterChain.doFilter(request, response);
    }

    private static final class AuthenticatedUser extends AbstractAuthenticationToken {

        private final UUID userId;

        private AuthenticatedUser(UUID userId) {
            super(Collections.emptyList());
            this.userId = userId;
            setAuthenticated(true);
        }

        @Override
        public Object getCredentials() {
            return "";
        }

        @Override
        public UUID getPrincipal() {
            return userId;
        }

        @Override
        public Collection<GrantedAuthority> getAuthorities() {
            return Collections.emptyList();
        }
    }
}
