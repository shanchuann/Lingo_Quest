package com.lingoquest.report.service;

import com.lingoquest.common.util.SecurityUtils;
import com.lingoquest.infra.ai.AiCache;
import com.lingoquest.infra.ai.DeepSeekClient;
import com.lingoquest.learning.service.LearningRecordService;
import com.lingoquest.report.dto.WeeklyReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyReportService {

    private final LearningRecordService recordService;
    private final DeepSeekClient deepSeek;
    private final AiCache cache;

    private static final String SYS = """
            You are an English learning coach for a Chinese student.
            Based on stats produced by a board-game English-learning app over the past 7 days,
            generate constructive feedback. Reply ONLY with JSON, no markdown fences.
            Schema:
            {"praise_zh": string,
             "weak_areas": [string, ...],
             "suggestions_zh": [string, string, string],
             "focus_next_week_zh": string}
            Praise and suggestions must be in Simplified Chinese.
            """;

    public WeeklyReport generate() {
        Long userId = SecurityUtils.currentUserId();
        var since = LocalDateTime.now().minusDays(7);
        var raw = recordService.statsByType(userId, since);

        int total = 0, correct = 0;
        var breakdowns = new ArrayList<WeeklyReport.TypeBreakdown>(raw.size());
        for (Map<String, Object> row : raw) {
            int t = toInt(row.get("total"));
            int c = toInt(row.get("correct"));
            total += t;
            correct += c;
            double acc = t == 0 ? 0.0 : Math.round((c * 1000.0 / t)) / 10.0;
            breakdowns.add(new WeeklyReport.TypeBreakdown(
                    String.valueOf(row.get("questionType")), t, c, acc));
        }

        double accuracy = total == 0 ? 0.0 : Math.round((correct * 1000.0 / total)) / 10.0;
        int wrong = total - correct;
        int xp = correct * 10 + wrong * 2;

        var stats = new WeeklyReport.Stats(total, correct, wrong, accuracy, xp);

        WeeklyReport.AiSummary summary;
        if (total == 0) {
            summary = new WeeklyReport.AiSummary(
                    "本周还没有答题记录，去试试 AI 桌游吧！",
                    List.of(),
                    List.of("先完成 5 道词汇题", "尝试 1 道语法题", "保持每天上线 5 分钟"),
                    "建立学习习惯");
        } else {
            String userPrompt = """
                    Total answers: %d
                    Correct: %d
                    Accuracy: %.1f%%
                    Type breakdown: %s
                    """.formatted(total, correct, accuracy, breakdowns);
            try {
                summary = cache.cached("report:" + userId,
                        userPrompt,
                        WeeklyReport.AiSummary.class,
                        () -> deepSeek.chatAsJson(SYS, userPrompt, 0.4, WeeklyReport.AiSummary.class));
            } catch (Exception e) {
                log.warn("AI summary unavailable, using fallback", e);
                summary = fallback(breakdowns, accuracy);
            }
        }

        return new WeeklyReport(stats, breakdowns, summary);
    }

    private WeeklyReport.AiSummary fallback(List<WeeklyReport.TypeBreakdown> bs, double acc) {
        var weak = new ArrayList<String>();
        for (var b : bs) if (b.accuracy() < 60) weak.add(b.type());
        var suggestions = new ArrayList<String>();
        suggestions.add("继续保持每日练习的节奏");
        if (!weak.isEmpty()) suggestions.add("重点复习：" + String.join("、", weak));
        else suggestions.add("尝试更高难度的题目以突破瓶颈");
        suggestions.add("使用 AI 词汇教练记录每天的新词");
        return new WeeklyReport.AiSummary(
                acc >= 80 ? "本周表现非常出色，继续保持！" : "本周已有不错的练习量，再接再厉！",
                weak,
                suggestions,
                weak.isEmpty() ? "挑战 CHALLENGE 题型" : ("加强 " + weak.get(0) + " 训练")
        );
    }

    private static int toInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number n) return n.intValue();
        try { return Integer.parseInt(o.toString()); } catch (Exception e) { return 0; }
    }
}
