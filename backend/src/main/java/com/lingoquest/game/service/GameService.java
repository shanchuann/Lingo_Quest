package com.lingoquest.game.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingoquest.common.exception.BizException;
import com.lingoquest.common.util.SecurityUtils;
import com.lingoquest.game.domain.Dice;
import com.lingoquest.game.domain.LevelPolicy;
import com.lingoquest.game.dto.*;
import com.lingoquest.game.entity.Game;
import com.lingoquest.game.entity.GameStatus;
import com.lingoquest.game.entity.Tile;
import com.lingoquest.game.event.AnswerSubmittedEvent;
import com.lingoquest.game.event.GameFinishedEvent;
import com.lingoquest.game.mapper.GameMapper;
import com.lingoquest.game.mapper.TileMapper;
import com.lingoquest.learning.entity.LearningRecord;
import com.lingoquest.learning.entity.Question;
import com.lingoquest.learning.service.LearningRecordService;
import com.lingoquest.learning.service.QuestionService;
import com.lingoquest.user.entity.User;
import com.lingoquest.user.mapper.UserMapper;
import com.lingoquest.shop.service.ShopService;
import com.lingoquest.wordbook.entity.Word;
import com.lingoquest.wordbook.mapper.UserWordProgressMapper;
import com.lingoquest.wordbook.mapper.WordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameMapper gameMapper;
    private final TileMapper tileMapper;
    private final UserMapper userMapper;
    private final QuestionService questionService;
    private final LearningRecordService recordService;
    private final WordMapper wordMapper;
    private final UserWordProgressMapper wordProgressMapper;
    private final ShopService shopService;
    private final ApplicationEventPublisher events;

    public List<TileView> board() {
        return tileMapper.selectList(null).stream()
                .sorted(Comparator.comparingInt(Tile::getPosition))
                .map(t -> new TileView(t.getPosition(), t.getTileType(), t.getTileName(),
                        t.getRewardExp(), t.getRewardCoin()))
                .toList();
    }

    @Transactional
    public GameView create() {
        var userId = currentUser();
        // Abandon any unfinished game first
        gameMapper.selectList(new LambdaQueryWrapper<Game>()
                .eq(Game::getUserId, userId)
                .in(Game::getStatus, GameStatus.CREATED.name(), GameStatus.IN_PROGRESS.name())
        ).forEach(g -> {
            g.setStatus(GameStatus.ABANDONED.name());
            g.setEndTime(LocalDateTime.now());
            gameMapper.updateById(g);
        });

        var game = Game.builder()
                .userId(userId)
                .currentPosition(0)
                .roundNum(0)
                .status(GameStatus.IN_PROGRESS.name())
                .totalScore(0)
                .startTime(LocalDateTime.now())
                .deleted(0)
                .build();
        gameMapper.insert(game);
        return toView(game);
    }

    public GameView current() {
        var userId = currentUser();
        var g = gameMapper.selectOne(new LambdaQueryWrapper<Game>()
                .eq(Game::getUserId, userId)
                .eq(Game::getStatus, GameStatus.IN_PROGRESS.name())
                .orderByDesc(Game::getId)
                .last("LIMIT 1"));
        if (g == null) throw new BizException(40410, "No active game, create one first");
        return toView(g);
    }

    @Transactional
    public RollDiceResponse rollDice(Long gameId) {
        var game = mustOwn(gameId);
        if (!GameStatus.IN_PROGRESS.name().equals(game.getStatus())) {
            throw new BizException("Game is not in progress");
        }
        int dice = Dice.roll();
        int from = game.getCurrentPosition();
        int to = Math.min(from + dice, LevelPolicy.BOARD_SIZE - 1);
        // Snapshot pre-roll position so RETRY can revert.
        game.setPreviousPosition(from);
        game.setCurrentPosition(to);
        game.setRoundNum(game.getRoundNum() + 1);

        var tile = tileMapper.selectOne(new LambdaQueryWrapper<Tile>().eq(Tile::getPosition, to));
        var tileView = new TileView(tile.getPosition(), tile.getTileType(), tile.getTileName(),
                tile.getRewardExp(), tile.getRewardCoin());

        QuestionView qView = null;
        boolean needsQuestion = switch (tile.getTileType()) {
            case "VOCAB", "GRAMMAR", "SPEAKING", "CHALLENGE" -> true;
            default -> false;
        };

        boolean finished = false;
        if ("END".equals(tile.getTileType())) {
            game.setStatus(GameStatus.FINISHED.name());
            game.setEndTime(LocalDateTime.now());
            applyReward(game.getUserId(), tile.getRewardExp(), tile.getRewardCoin());
            finished = true;
        } else if ("LUCKY".equals(tile.getTileType())) {
            applyReward(game.getUserId(), tile.getRewardExp(), tile.getRewardCoin());
            game.setTotalScore(game.getTotalScore() + tile.getRewardExp());
        } else if ("TRAP".equals(tile.getTileType())) {
            applyReward(game.getUserId(), tile.getRewardExp(), 0); // exp is negative
            game.setTotalScore(Math.max(0, game.getTotalScore() + tile.getRewardExp()));
        }

        if (needsQuestion) {
            Question q = null;
            // If on VOCAB tile and the user has picked a word book, build the
            // question from their book so the gameplay loop reinforces study.
            if ("VOCAB".equals(tile.getTileType())) {
                var u = userMapper.selectById(game.getUserId());
                if (u != null && u.getCurrentBookId() != null) {
                    q = questionFromBook(u.getCurrentBookId());
                }
            }
            if (q == null) {
                q = questionService.randomByTileType(tile.getTileType());
            }
            qView = new QuestionView(q.getId(), q.getType(), q.getContent(), q.getOptions(), q.getDifficulty());
        }

        gameMapper.updateById(game);

        if (finished) {
            events.publishEvent(new GameFinishedEvent(game.getUserId(), game.getId(),
                    game.getTotalScore(), true));
        }

        return new RollDiceResponse(dice, from, to, tileView, qView, finished);
    }

    @Transactional
    public AnswerResponse answer(Long gameId, AnswerRequest req) {
        var game = mustOwn(gameId);
        var question = questionService.getById(req.questionId());
        boolean correct = normalize(question.getAnswer()).equals(normalize(req.answer()));
        int gain = correct ? LevelPolicy.EXP_RIGHT : LevelPolicy.EXP_WRONG;

        recordService.save(LearningRecord.builder()
                .userId(game.getUserId())
                .questionId(question.getId())
                .gameId(game.getId())
                .userAnswer(req.answer())
                .isCorrect(correct)
                .score(gain)
                .createdAt(LocalDateTime.now())
                .build());

        var user = userMapper.selectById(game.getUserId());
        int before = user.getLevel();
        // XP_DOUBLE buff — applied on correct answer, then cleared.
        boolean usedXpDouble = false;
        if (correct && user.getXpDoublePending() != null && user.getXpDoublePending() == 1) {
            gain = gain * 2;
            user.setXpDoublePending(0);
            usedXpDouble = true;
        }
        user.setExperience(user.getExperience() + gain);
        int after = LevelPolicy.levelOf(user.getExperience());
        user.setLevel(after);
        user.setCoins(user.getCoins() + (correct ? 5 : 1));
        userMapper.updateById(user);

        // Word-book progress sync: if this question came from the wordbook
        // path (tags="wordbook:<wordId>"), mark the linked word.
        if (question.getTags() != null && question.getTags().startsWith("wordbook:")) {
            try {
                Long wordId = Long.parseLong(question.getTags().substring("wordbook:".length()));
                wordProgressMapper.upsert(game.getUserId(), wordId,
                        correct ? "MASTERED" : "LEARNING");
            } catch (NumberFormatException ignored) { /* tag malformed; skip */ }
        }
        // (usedXpDouble is reflected in the gain value already — UI knows from total exp delta)
        if (usedXpDouble) {
            // no-op; gain doubled above.
        }

        game.setTotalScore(game.getTotalScore() + gain);
        gameMapper.updateById(game);

        events.publishEvent(new AnswerSubmittedEvent(
                game.getUserId(), game.getId(), question.getId(),
                question.getType(), correct, gain, user.getExperience(), after, before
        ));

        return new AnswerResponse(correct, question.getAnswer(), gain,
                user.getExperience(), after, after > before);
    }

    @Transactional
    public GameView abandon(Long gameId) {
        var game = mustOwn(gameId);
        game.setStatus(GameStatus.ABANDONED.name());
        game.setEndTime(LocalDateTime.now());
        gameMapper.updateById(game);
        events.publishEvent(new GameFinishedEvent(game.getUserId(), game.getId(),
                game.getTotalScore(), false));
        return toView(game);
    }

    private Game mustOwn(Long gameId) {
        var g = gameMapper.selectById(gameId);
        if (g == null) throw new BizException(40410, "Game not found");
        if (!g.getUserId().equals(currentUser())) throw new BizException(40300, "Not your game");
        return g;
    }

    // ─── Functional shop item endpoints ─────────────────────

    /** Consume one HINT item; return a masked-letter hint for the correct answer. */
    @Transactional
    public HintResponse useHint(Long gameId, Long questionId) {
        var game = mustOwn(gameId);
        var q = questionService.getById(questionId);
        shopService.consumeOneByEffect(game.getUserId(), "HINT");
        String answer = q.getAnswer() == null ? "" : q.getAnswer().trim();
        String hint;
        if (answer.isEmpty()) {
            hint = "答案为空,无法生成提示";
        } else if (q.getOptions() != null && !q.getOptions().isEmpty()) {
            // For multi-choice: hint = first letter of correct option.
            hint = "正确选项以「" + answer.charAt(0) + "」开头";
        } else {
            // For free-text: reveal first char + length.
            String rest = answer.length() > 1 ? "_".repeat(answer.length() - 1) : "";
            hint = "首字母:" + answer.charAt(0) + " · 共 " + answer.length() + " 字 · " + answer.charAt(0) + rest;
        }
        return new HintResponse(questionId, hint, "已使用 1 张提示卷");
    }

    /** Consume one SKIP item; clear the pending question with no score / no learning record. */
    @Transactional
    public AnswerResponse useSkip(Long gameId, Long questionId) {
        var game = mustOwn(gameId);
        var q = questionService.getById(questionId);
        shopService.consumeOneByEffect(game.getUserId(), "SKIP");
        var user = userMapper.selectById(game.getUserId());
        return new AnswerResponse(false, q.getAnswer(), 0,
                user.getExperience(), user.getLevel(), false);
    }

    /** Consume one RETRY item; revert player to previous position and roll again. */
    @Transactional
    public RollDiceResponse useRetry(Long gameId) {
        var game = mustOwn(gameId);
        if (game.getPreviousPosition() == null) {
            throw new BizException(40006, "还没掷过骰子,没法重掷");
        }
        shopService.consumeOneByEffect(game.getUserId(), "RETRY");
        // Revert and reset round so the rollDice() flow runs cleanly.
        game.setCurrentPosition(game.getPreviousPosition());
        game.setRoundNum(Math.max(0, game.getRoundNum() - 1));
        gameMapper.updateById(game);
        return rollDice(gameId);
    }

    /** Pick a word + 3 distractor meanings from the same book to form a question. */
    private Question questionFromBook(Integer bookId) {
        Word target = wordMapper.pickRandom(bookId);
        if (target == null) return null;
        // Get a few extra random rows and filter to distinct meanings for distractors.
        var pool = wordMapper.selectList(new LambdaQueryWrapper<Word>()
                .eq(Word::getBookId, bookId)
                .ne(Word::getId, target.getId())
                .last("ORDER BY RAND() LIMIT 6"));
        var distractors = pool.stream()
                .map(Word::getMeaningZh)
                .filter(m -> m != null && !m.equals(target.getMeaningZh()))
                .distinct()
                .limit(3)
                .toList();
        if (distractors.size() < 3) return null;     // fall back to question pool
        return questionService.createFromWord(target.getId(), target.getWord(), target.getMeaningZh(), distractors);
    }


    private void applyReward(Long userId, int exp, int coin) {
        var u = userMapper.selectById(userId);
        if (u == null) return;
        u.setExperience(Math.max(0, u.getExperience() + exp));
        u.setCoins(Math.max(0, u.getCoins() + coin));
        u.setLevel(LevelPolicy.levelOf(u.getExperience()));
        userMapper.updateById(u);
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private static Long currentUser() {
        var id = SecurityUtils.currentUserId();
        if (id == null) throw new BizException(40100, "Not authenticated");
        return id;
    }

    private static GameView toView(Game g) {
        return new GameView(g.getId(), g.getUserId(), g.getCurrentPosition(),
                g.getRoundNum(), g.getStatus(), g.getTotalScore());
    }
}
