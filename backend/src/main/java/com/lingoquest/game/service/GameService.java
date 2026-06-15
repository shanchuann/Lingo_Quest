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
            Question q = questionService.randomByTileType(tile.getTileType());
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
        user.setExperience(user.getExperience() + gain);
        int after = LevelPolicy.levelOf(user.getExperience());
        user.setLevel(after);
        user.setCoins(user.getCoins() + (correct ? 5 : 1));
        userMapper.updateById(user);

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
