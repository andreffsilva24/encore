package com.encore.user_service.dto;

import java.util.UUID;

public record LoginResponse(
        UUID userId,
        String name,
        String email
) {}