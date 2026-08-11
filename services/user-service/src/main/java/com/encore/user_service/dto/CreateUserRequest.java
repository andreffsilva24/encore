package com.encore.user_service.dto;

import java.util.UUID;

public record CreateUserRequest(
    String name,
    String email
) {}
