package com.lingoquest.ranking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingoquest.game.event.AnswerSubmittedEvent;
import com.lingoquest.user.entity.User;
import com.lingoquest.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final WeeklyLeaderboard weeklyLeaderboard;
    private final UserMapper userMapper;

    @Async
    @EventListener
    public void onAnswer(AnswerSubmittedEvent e) {
        if (e.correct()) {
            weeklyLeaderboard.record(e.userId(), e.gainedExp());
        }
    }

    public List<Map<String, Object>> weeklyTop(int limit) {
        var entries = weeklyLeaderboard.top(limit);
        if (entries.isEmpty()) return List.of();
        var result = new ArrayList<Map<String, Object>>(entries.size());
        int rank = 1;
        for (var entry : entries) {
            var u = userMapper.selectById(entry.userId());
            if (u == null) continue;
            var row = new LinkedHashMap<String, Object>();
            row.put("rank", rank++);
            row.put("userId", u.getId());
            row.put("username", u.getUsername());
            row.put("level", u.getLevel());
            row.put("score", entry.score());
            result.add(row);
        }
        return result;
    }

    public List<Map<String, Object>> allTimeTop(int limit) {
        var users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .orderByDesc(User::getExperience)
                .last("LIMIT " + Math.max(1, Math.min(limit, 100))));
        var result = new ArrayList<Map<String, Object>>(users.size());
        int rank = 1;
        for (User u : users) {
            var row = new LinkedHashMap<String, Object>();
            row.put("rank", rank++);
            row.put("userId", u.getId());
            row.put("username", u.getUsername());
            row.put("level", u.getLevel());
            row.put("experience", u.getExperience());
            result.add(row);
        }
        return result;
    }
}
