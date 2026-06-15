package com.lingoquest.ai.service;

import com.lingoquest.ai.dto.GrammarCheckRequest;
import com.lingoquest.ai.dto.GrammarCheckResponse;
import com.lingoquest.ai.dto.VocabCoachRequest;
import com.lingoquest.ai.dto.VocabCoachResponse;
import com.lingoquest.common.util.SecurityUtils;
import com.lingoquest.infra.ai.AiCache;
import com.lingoquest.infra.ai.AiRateLimiter;
import com.lingoquest.infra.ai.DeepSeekClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiTutorService {

    private final DeepSeekClient deepSeek;
    private final AiRateLimiter rateLimiter;
    private final AiCache cache;

    private static final String GRAMMAR_SYS = """
            You are a strict but encouraging English teacher.
            Correct grammar errors in the given sentence.
            Reply ONLY with JSON, no prose, no markdown fences.
            Schema: {"original": string, "corrected": string, "explanation_zh": string, "is_correct": boolean}
            If the input is already correct, set is_correct=true and corrected=original.
            The explanation_zh must be in Simplified Chinese.
            """;

    private static final String VOCAB_SYS = """
            You are an English vocabulary coach for Chinese learners.
            Given a word, produce its phonetic, part of speech, Chinese meaning, English meaning,
            two short example sentences, three synonyms, and a memory tip in Chinese.
            Reply ONLY with JSON, no prose, no markdown fences.
            Schema: {"word": string, "phonetic": string, "part_of_speech": string,
                     "meaning_zh": string, "meaning_en": string,
                     "examples": [string, string], "synonyms": [string, string, string],
                     "memory_tip": string}
            """;

    public GrammarCheckResponse correctGrammar(GrammarCheckRequest req) {
        rateLimiter.check(SecurityUtils.currentUserId());
        return cache.cached("grammar", req.sentence(), GrammarCheckResponse.class,
                () -> deepSeek.chatAsJson(GRAMMAR_SYS, req.sentence(), 0.2, GrammarCheckResponse.class));
    }

    public VocabCoachResponse coachVocab(VocabCoachRequest req) {
        rateLimiter.check(SecurityUtils.currentUserId());
        var word = req.word().trim().toLowerCase();
        return cache.cached("vocab", word, VocabCoachResponse.class,
                () -> deepSeek.chatAsJson(VOCAB_SYS, word, 0.3, VocabCoachResponse.class));
    }
}
