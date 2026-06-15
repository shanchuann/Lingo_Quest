package com.lingoquest.ai.controller;

import com.lingoquest.ai.dto.GrammarCheckRequest;
import com.lingoquest.ai.dto.GrammarCheckResponse;
import com.lingoquest.ai.dto.VocabCoachRequest;
import com.lingoquest.ai.dto.VocabCoachResponse;
import com.lingoquest.ai.service.AiTutorService;
import com.lingoquest.common.api.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI Tutor")
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiTutorController {
    private final AiTutorService aiTutorService;

    @Operation(summary = "Correct English grammar")
    @PostMapping("/grammar")
    public R<GrammarCheckResponse> grammar(@Valid @RequestBody GrammarCheckRequest req) {
        return R.ok(aiTutorService.correctGrammar(req));
    }

    @Operation(summary = "Explain an English word")
    @PostMapping("/vocab")
    public R<VocabCoachResponse> vocab(@Valid @RequestBody VocabCoachRequest req) {
        return R.ok(aiTutorService.coachVocab(req));
    }
}
