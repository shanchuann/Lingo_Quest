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

    /** Build a 4-choice VOCAB question from a word + 3 distractor meanings,
     *  persist it (so the answer endpoint can look it up), and return it.
     *  The tag carries the originating wordId so GameService can sync progress. */
    public Question createFromWord(Long wordId, String word, String correctMeaning, List<String> distractors) {
        var options = new java.util.ArrayList<String>(4);
        options.add(correctMeaning);
        options.addAll(distractors);
        java.util.Collections.shuffle(options);
        Question q = Question.builder()
                .type("VOCAB")
                .content("单词「" + word + "」的中文含义是?")
                .options(options)
                .answer(correctMeaning)
                .difficulty(2)
                .tags("wordbook:" + wordId)
                .deleted(0)
                .build();
        questionMapper.insert(q);
        return q;
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
