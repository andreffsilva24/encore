package com.encore.user_service.user;

import com.encore.user_service.dto.CreateUserRequest;
import com.encore.user_service.dto.LoginRequest;
import com.encore.user_service.dto.LoginResponse;
import com.encore.user_service.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    @Transactional
    public User createUser(@NonNull CreateUserRequest createUserRequest) {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setName(createUserRequest.name());
        user.setEmail(createUserRequest.email());
        user.setPasswordHash(passwordEncoder.encode(createUserRequest.password()));
        user.setCreatedAt(OffsetDateTime.now());
        userRepository.save(user);

        log.info("[USER] User {} created with email {}", user.getUserId(), user.getEmail());
        return user;
    }

    public static UserResponse toResponse(@NonNull User user) {
        return new UserResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }

    public Optional<LoginResponse> login(@NonNull LoginRequest request) {
        return findUserByEmail(request.email())
                .filter(user -> passwordEncoder.matches(request.password(), user.getPasswordHash()))
                .map(user -> new LoginResponse(user.getUserId(), user.getName(), user.getEmail()));
    }

    public Optional<User> findUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        user.ifPresentOrElse(
                u -> log.info("[USER] User {} found with email {}", u.getUserId(), email),
                () -> log.warn("[USER] User with email {} not found", email)
        );
        return user;
    }

    public Optional<User> findUserById(UUID userId) {
        Optional<User> user = userRepository.findById(userId);
        user.ifPresentOrElse(
                u -> log.info("[USER] User {} found", userId),
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
