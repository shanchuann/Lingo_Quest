package com.lingoquest.achievement.controller;

import com.lingoquest.achievement.entity.Achievement;
import com.lingoquest.achievement.service.AchievementService;
import com.lingoquest.common.api.R;
import com.lingoquest.common.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "Achievement")
@RestController
@RequestMapping("/achievements")
@RequiredArgsConstructor
public class AchievementController {
    private final AchievementService achievementService;

    @Operation(summary = "List all achievements in the catalog")
    @GetMapping
    public R<List<Achievement>> catalog() {
        return R.ok(achievementService.catalog());
    }

    @Operation(summary = "List achievements unlocked by current user")
    @GetMapping("/mine")
    public R<List<Map<String, Object>>> mine() {
        return R.ok(achievementService.listUnlocked(SecurityUtils.currentUserId()));
    }
}
