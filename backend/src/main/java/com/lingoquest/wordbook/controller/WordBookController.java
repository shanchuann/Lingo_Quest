package com.lingoquest.wordbook.controller;

import com.lingoquest.common.api.R;
import com.lingoquest.common.util.SecurityUtils;
import com.lingoquest.wordbook.dto.MarkRequest;
import com.lingoquest.wordbook.entity.Word;
import com.lingoquest.wordbook.service.WordBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "Word book")
@RestController
@RequestMapping("/wordbooks")
@RequiredArgsConstructor
public class WordBookController {

    private final WordBookService service;

    @Operation(summary = "List all word books with this user's progress")
    @GetMapping
    public R<List<Map<String, Object>>> books() {
        return R.ok(service.listBooks(SecurityUtils.currentUserId()));
    }

    @Operation(summary = "List words inside a book, with the user's mark state")
    @GetMapping("/{bookId}/words")
    public R<List<Map<String, Object>>> words(@PathVariable Integer bookId,
                                              @RequestParam(required = false) String state) {
        return R.ok(service.listWords(SecurityUtils.currentUserId(), bookId, state));
    }

    @Operation(summary = "Mark a word with NEW | LEARNING | MASTERED")
    @PostMapping("/words/{wordId}/mark")
    public R<Void> mark(@PathVariable Long wordId, @Valid @RequestBody MarkRequest req) {
        service.mark(SecurityUtils.currentUserId(), wordId, req.state());
        return R.ok();
    }

    @Operation(summary = "Select a book as the user's current book (or null to clear)")
    @PostMapping("/select")
    public R<Void> select(@RequestParam(required = false) Integer bookId) {
        service.selectBook(SecurityUtils.currentUserId(), bookId);
        return R.ok();
    }

    @Operation(summary = "Random word from a given book, or null from user's current book")
    @GetMapping("/random")
    public R<Word> random(@RequestParam(required = false) Integer bookId) {
        if (bookId != null) return R.ok(service.randomFromBook(bookId));
        return R.ok(service.randomFromCurrent(SecurityUtils.currentUserId()));
    }
}
