package com.lingoquest.game.controller;

import com.lingoquest.common.api.R;
import com.lingoquest.game.dto.*;
import com.lingoquest.game.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Game")
@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;

    @Operation(summary = "Get the 30-tile board")
    @GetMapping("/board")
    public R<List<TileView>> board() {
        return R.ok(gameService.board());
    }

    @Operation(summary = "Create a new game (abandons any active one)")
    @PostMapping
    public R<GameView> create() {
        return R.ok(gameService.create());
    }

    @Operation(summary = "Get my current in-progress game")
    @GetMapping("/current")
    public R<GameView> current() {
        return R.ok(gameService.current());
    }

    @Operation(summary = "Roll the dice")
    @PostMapping("/{id}/roll")
    public R<RollDiceResponse> roll(@PathVariable Long id) {
        return R.ok(gameService.rollDice(id));
    }

    @Operation(summary = "Submit an answer for the current tile")
    @PostMapping("/{id}/answer")
    public R<AnswerResponse> answer(@PathVariable Long id, @Valid @RequestBody AnswerRequest req) {
        return R.ok(gameService.answer(id, req));
    }

    @Operation(summary = "Abandon the game")
    @PostMapping("/{id}/abandon")
    public R<GameView> abandon(@PathVariable Long id) {
        return R.ok(gameService.abandon(id));
    }

    @Operation(summary = "Use a 提示卷 to reveal a hint for the current question")
    @PostMapping("/{id}/hint")
    public R<HintResponse> hint(@PathVariable Long id, @RequestParam Long questionId) {
        return R.ok(gameService.useHint(id, questionId));
    }

    @Operation(summary = "Use a 越格符 to skip the current question with no score")
    @PostMapping("/{id}/skip")
    public R<AnswerResponse> skip(@PathVariable Long id, @RequestParam Long questionId) {
        return R.ok(gameService.useSkip(id, questionId));
    }

    @Operation(summary = "Use a 重掷令 to revert position and roll again")
    @PostMapping("/{id}/retry")
    public R<RollDiceResponse> retry(@PathVariable Long id) {
        return R.ok(gameService.useRetry(id));
    }
}
