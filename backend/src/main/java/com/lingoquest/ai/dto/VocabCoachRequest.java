package com.lingoquest.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VocabCoachRequest(
        @NotBlank @Size(max = 64) String word
) {}
