package com.lingoquest.wordbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MarkRequest(
        @NotBlank
        @Pattern(regexp = "NEW|LEARNING|MASTERED", message = "state must be NEW | LEARNING | MASTERED")
        String state
) {}
