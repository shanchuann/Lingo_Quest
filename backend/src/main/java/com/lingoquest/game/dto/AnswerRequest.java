package com.lingoquest.game.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnswerRequest(
        @NotNull Long questionId,
        @NotBlank String answer
) {}
