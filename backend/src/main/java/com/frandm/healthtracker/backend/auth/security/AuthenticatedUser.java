package com.frandm.healthtracker.backend.auth.security;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class AuthenticatedUser extends AbstractAuthenticationToken {

    private final UUID userId;

    public AuthenticatedUser(UUID userId) {
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
