package com.lingoquest.achievement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingoquest.achievement.entity.Achievement;
import com.lingoquest.achievement.entity.UserAchievement;
import com.lingoquest.achievement.mapper.AchievementMapper;
import com.lingoquest.achievement.mapper.UserAchievementMapper;
import com.lingoquest.game.event.AnswerSubmittedEvent;
import com.lingoquest.game.event.GameFinishedEvent;
import com.lingoquest.learning.service.LearningRecordService;
import com.lingoquest.user.entity.User;
import com.lingoquest.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementMapper achievementMapper;
    private final UserAchievementMapper userAchievementMapper;
    private final UserMapper userMapper;
    private final LearningRecordService recordService;

    private final ThreadLocal<Set<Long>> streakCache = ThreadLocal.withInitial(HashSet::new);

    @Async
    @EventListener
    public void onAnswer(AnswerSubmittedEvent e) {
        try {
            if (e.correct()) {
                tryUnlock(e.userId(), "FIRST_STEP");

                long totalCorrect = recordService.countCorrect(e.userId());
                if (totalCorrect >= 100) tryUnlock(e.userId(), "CENTURION");

                long vocabCorrect = recordService.countCorrectByType(e.userId(), "VOCAB");
                if (vocabCorrect >= 20) tryUnlock(e.userId(), "VOCAB_MASTER");

                long grammarCorrect = recordService.countCorrectByType(e.userId(), "GRAMMAR");
                if (grammarCorrect >= 20) tryUnlock(e.userId(), "GRAMMAR_PRO");
            }
            if (e.levelAfter() > e.levelBefore() && e.levelAfter() >= 3) {
                tryUnlock(e.userId(), "LEVEL_UP");
            }
        } catch (Exception ex) {
            log.warn("achievement listener failed", ex);
        }
    }

    @Async
    @EventListener
    public void onGameFinished(GameFinishedEvent e) {
        if (e.reachedEnd()) {
            tryUnlock(e.userId(), "CHAMPION");
        }
    }

    public List<java.util.Map<String, Object>> listUnlocked(Long userId) {
        return userAchievementMapper.listUnlocked(userId);
    }

    public List<Achievement> catalog() {
        return achievementMapper.selectList(null);
    }

    /** Manual unlock entrypoint for the shop accelerator items. */
    public void unlockManually(Long userId, String code) {
        tryUnlock(userId, code);
    }

    private void tryUnlock(Long userId, String code) {
        var ach = achievementMapper.selectOne(new LambdaQueryWrapper<Achievement>().eq(Achievement::getCode, code));
        if (ach == null) return;
        var existing = userAchievementMapper.selectCount(
                new LambdaQueryWrapper<UserAchievement>()
                        .eq(UserAchievement::getUserId, userId)
                        .eq(UserAchievement::getAchievementId, ach.getId())
        );
        if (existing > 0) return;
        userAchievementMapper.insert(new UserAchievement(userId, ach.getId(), LocalDateTime.now()));
        // Reward
        User u = userMapper.selectById(userId);
        if (u != null) {
            u.setExperience(u.getExperience() + ach.getRewardExp());
            u.setCoins(u.getCoins() + ach.getRewardCoin());
            userMapper.updateById(u);
        }
        log.info("unlocked achievement {} for user {}", code, userId);
    }
}
