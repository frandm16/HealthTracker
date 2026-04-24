package com.frandm.healthtracker.backend.auth.repository;

import com.frandm.healthtracker.backend.auth.model.UserEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
}
