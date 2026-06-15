package com.lingoquest.game.dto;

public record RollDiceResponse(
        int dice,
        int fromPosition,
        int toPosition,
        TileView landed,
        QuestionView question,
        boolean finished
) {}
