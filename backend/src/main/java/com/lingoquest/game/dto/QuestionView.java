package com.lingoquest.game.dto;

import java.util.List;

public record QuestionView(
        Long id,
        String type,
        String content,
        List<String> options,
        int difficulty
) {}
