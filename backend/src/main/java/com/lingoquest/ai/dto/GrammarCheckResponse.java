package com.lingoquest.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public record GrammarCheckResponse(
        String original,
        String corrected,
        @JsonProperty("explanation_zh") String explanationZh,
        @JsonProperty("is_correct") Boolean isCorrect
) implements Serializable {}
