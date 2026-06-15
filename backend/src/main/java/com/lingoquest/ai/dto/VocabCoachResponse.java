package com.lingoquest.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public record VocabCoachResponse(
        String word,
        String phonetic,
        @JsonProperty("part_of_speech") String partOfSpeech,
        @JsonProperty("meaning_zh") String meaningZh,
        @JsonProperty("meaning_en") String meaningEn,
        List<String> examples,
        List<String> synonyms,
        @JsonProperty("memory_tip") String memoryTip
) implements Serializable {}
