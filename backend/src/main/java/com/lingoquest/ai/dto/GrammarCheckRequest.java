package com.lingoquest.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GrammarCheckRequest(
        @NotBlank @Size(max = 500) String sentence
) {}
