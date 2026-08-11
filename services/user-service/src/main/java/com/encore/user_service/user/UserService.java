package com.encore.user_service.user;

import com.encore.user_service.dto.CreateUserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User createUser(CreateUserRequest createUserRequest) {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setName(createUserRequest.name());
        user.setEmail(createUserRequest.email());
        user.setCreatedAt(OffsetDateTime.now());
        userRepository.save(user);


        log.info("[USER] User {} created with email {}", user.getUserId(), user.getEmail());
        return user;
    }

    public Optional<User> findUserById(UUID userId) {
        Optional<User> user = userRepository.findById(userId);
        user.ifPresentOrElse(
                u -> log.info("[USER] User {} found with email {}", userId, u.getEmail()),
                () -> log.warn("[USER] User {} not found", userId)
        );
        return user;
    }

    public List<User> findAllUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty())
            log.info("[USER] No users found");
        else
            log.info("[USER] {} users found", users.size());
        return users;
    }
}
