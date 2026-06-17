package com.lingoquest.game.dto;

public record HintResponse(
        Long questionId,
        String hint,
        String message
) {}
