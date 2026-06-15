package com.lingoquest.ranking.controller;

import com.lingoquest.common.api.R;
import com.lingoquest.ranking.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "Ranking")
@RestController
@RequestMapping("/ranking")
@RequiredArgsConstructor
public class RankingController {
    private final RankingService rankingService;

    @Operation(summary = "Weekly top players (Redis ZSET)")
    @GetMapping("/weekly")
    public R<List<Map<String, Object>>> weekly(@RequestParam(defaultValue = "20") int limit) {
        return R.ok(rankingService.weeklyTop(limit));
    }

    @Operation(summary = "All-time top players (SQL)")
    @GetMapping("/all-time")
    public R<List<Map<String, Object>>> allTime(@RequestParam(defaultValue = "20") int limit) {
        return R.ok(rankingService.allTimeTop(limit));
    }
}
