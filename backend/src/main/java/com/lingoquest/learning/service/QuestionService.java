package com.lingoquest.learning.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingoquest.common.exception.BizException;
import com.lingoquest.learning.entity.Question;
import com.lingoquest.learning.mapper.QuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionMapper questionMapper;

    public Question randomByTileType(String tileType) {
        var type = mapTileToQuestionType(tileType);
        var pool = questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .eq(Question::getType, type));
        if (pool.isEmpty()) throw new BizException(50010, "No question available for tile " + tileType);
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    public Question getById(Long id) {
        var q = questionMapper.selectById(id);
        if (q == null) throw new BizException(40400, "Question not found");
        return q;
    }

    public List<Question> all() {
        return questionMapper.selectList(null);
    }

    private static String mapTileToQuestionType(String tileType) {
        return switch (tileType) {
            case "GRAMMAR"   -> "GRAMMAR";
            case "SPEAKING"  -> "READING";
            case "CHALLENGE" -> "READING";
            default          -> "VOCAB";
        };
    }
}
