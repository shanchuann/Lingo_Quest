package com.lingoquest.learning.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingoquest.learning.entity.LearningRecord;
import com.lingoquest.learning.mapper.LearningRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LearningRecordService {
    private final LearningRecordMapper mapper;

    public void save(LearningRecord record) {
        mapper.insert(record);
    }

    public long countCorrect(Long userId) {
        return mapper.selectCount(new LambdaQueryWrapper<LearningRecord>()
                .eq(LearningRecord::getUserId, userId)
                .eq(LearningRecord::getIsCorrect, true));
    }

    public long countCorrectByType(Long userId, String type) {
        return mapper.selectCount(new LambdaQueryWrapper<LearningRecord>()
                .eq(LearningRecord::getUserId, userId)
                .eq(LearningRecord::getIsCorrect, true)
                .inSql(LearningRecord::getQuestionId,
                        "SELECT id FROM question WHERE type = '" + type.replace("'", "") + "'"));
    }

    public List<LearningRecord> recentByUser(Long userId, int limit) {
        return mapper.selectList(new LambdaQueryWrapper<LearningRecord>()
                .eq(LearningRecord::getUserId, userId)
                .orderByDesc(LearningRecord::getCreatedAt)
                .last("LIMIT " + Math.max(1, Math.min(limit, 200))));
    }

    public List<Map<String, Object>> statsByType(Long userId, LocalDateTime since) {
        return mapper.statsByType(userId, since);
    }
}
