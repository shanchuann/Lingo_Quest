package com.lingoquest.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public record WeeklyReport(
        Stats stats,
        @JsonProperty("by_type") List<TypeBreakdown> byType,
        @JsonProperty("ai_summary") AiSummary aiSummary
) implements Serializable {

    public record Stats(
            int totalAnswers,
            int correctAnswers,
            int wrongAnswers,
            double accuracy,
            int xpGained
    ) implements Serializable {}

    public record TypeBreakdown(
            String type,
            int total,
            int correct,
            double accuracy
    ) implements Serializable {}

    public record AiSummary(
            @JsonProperty("praise_zh") String praiseZh,
            @JsonProperty("weak_areas") List<String> weakAreas,
            @JsonProperty("suggestions_zh") List<String> suggestionsZh,
            @JsonProperty("focus_next_week_zh") String focusNextWeekZh
    ) implements Serializable {}
}
