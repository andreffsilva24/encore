package com.encore.user_service.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(
        UUID userId,
        String name,
        String email,
        OffsetDateTime createdAt
) { }