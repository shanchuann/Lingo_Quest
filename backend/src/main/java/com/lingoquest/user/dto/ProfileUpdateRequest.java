package com.lingoquest.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @Email @Size(max = 100) String email,
        @Size(max = 255) String avatar
) {}
