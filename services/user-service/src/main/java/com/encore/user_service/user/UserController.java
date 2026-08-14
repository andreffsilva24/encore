package com.encore.user_service.user;

import com.encore.user_service.dto.CreateUserRequest;
import com.encore.user_service.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping()
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest createUserRequest) {
        return ResponseEntity.status(201).body(
                UserService.toResponse(userService.createUser(createUserRequest))
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable UUID userId) {
        return userService.findUserById(userId)
                .map(user -> ResponseEntity.ok(UserService.toResponse(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping()
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(
                userService.findAllUsers().stream()
                        .map(UserService::toResponse)
                        .toList()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }
}
