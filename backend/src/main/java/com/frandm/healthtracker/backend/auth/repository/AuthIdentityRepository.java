package com.frandm.healthtracker.backend.auth.repository;

import com.frandm.healthtracker.backend.auth.model.AuthIdentityEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthIdentityRepository extends JpaRepository<AuthIdentityEntity, UUID> {

    Optional<AuthIdentityEntity> findByProviderAndProviderUserId(String provider, String providerUserId);
}
