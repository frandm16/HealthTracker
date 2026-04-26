package com.frandm.healthtracker.backend.nutrition.common;

import com.frandm.healthtracker.backend.auth.model.UserEntity;
import com.frandm.healthtracker.backend.auth.repository.UserRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NutritionAccessService {

    private final UserRepository userRepository;

    public NutritionAccessService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found."));
    }

    public void assertOwned(UUID expectedUserId, UUID actualUserId) {
        if (!expectedUserId.equals(actualUserId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found.");
        }
    }
}
