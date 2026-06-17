package com.lingoquest.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingoquest.common.exception.BizException;
import com.lingoquest.common.util.JwtTokenProvider;
import com.lingoquest.user.dto.AuthResponse;
import com.lingoquest.user.dto.LoginRequest;
import com.lingoquest.user.dto.PasswordChangeRequest;
import com.lingoquest.user.dto.ProfileUpdateRequest;
import com.lingoquest.user.dto.RegisterRequest;
import com.lingoquest.user.dto.UserProfile;
import com.lingoquest.user.dto.UserStats;
import com.lingoquest.user.entity.User;
import com.lingoquest.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwt;

    @Value("${app.jwt.expire-minutes}")
    private long expireMinutes;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (existsByUsername(req.username())) throw new BizException("Username already taken");
        if (existsByEmail(req.email()))       throw new BizException("Email already registered");

        var user = User.builder()
                .username(req.username())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .level(1)
                .experience(0)
                .coins(0)
                .deleted(0)
                .build();
        userMapper.insert(user);
        return issueToken(user);
    }

    public AuthResponse login(LoginRequest req) {
        var user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.username()));
        if (user == null || !passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new BizException(40100, "Invalid username or password");
        }
        return issueToken(user);
    }

    public UserProfile me(Long userId) {
        var u = userMapper.selectById(userId);
        if (u == null) throw new BizException(40400, "User not found");
        return UserProfile.of(u);
    }

    @Transactional
    public UserProfile updateProfile(Long userId, ProfileUpdateRequest req) {
        var u = userMapper.selectById(userId);
        if (u == null) throw new BizException(40400, "User not found");

        if (StringUtils.hasText(req.email()) && !req.email().equalsIgnoreCase(u.getEmail())) {
            if (existsByEmail(req.email())) throw new BizException(40009, "邮箱已被使用");
            u.setEmail(req.email());
        }
        if (req.avatar() != null) {
            u.setAvatar(req.avatar().isBlank() ? null : req.avatar());
        }
        userMapper.updateById(u);
        return UserProfile.of(u);
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest req) {
        var u = userMapper.selectById(userId);
        if (u == null) throw new BizException(40400, "User not found");
        if (!passwordEncoder.matches(req.oldPassword(), u.getPassword())) {
            throw new BizException(40101, "原密码不正确");
        }
        u.setPassword(passwordEncoder.encode(req.newPassword()));
        userMapper.updateById(u);
    }

    public UserStats stats(Long userId) {
        var u = userMapper.selectById(userId);
        if (u == null) throw new BizException(40400, "User not found");
        Map<String, Object> row = userMapper.aggregateStats(userId);
        long totalGames     = num(row, "totalGames");
        long finishedGames  = num(row, "finishedGames");
        long totalAnswers   = num(row, "totalAnswers");
        long correctAnswers = num(row, "correctAnswers");
        double accuracy = totalAnswers > 0 ? (correctAnswers * 100.0) / totalAnswers : 0.0;
        return new UserStats(
                totalGames, finishedGames, totalAnswers, correctAnswers,
                Math.round(accuracy * 10.0) / 10.0,
                u.getLevel(), u.getExperience(), u.getCoins());
    }

    public java.util.List<java.util.Map<String, Object>> recentGames(Long userId, int limit) {
        return userMapper.recentGames(userId, limit);
    }

    public java.util.List<java.util.Map<String, Object>> dailyAccuracy(Long userId, int days) {
        return userMapper.dailyAccuracy(userId, days);
    }

    @Transactional
    public void deleteSelf(Long userId) {
        var u = userMapper.selectById(userId);
        if (u == null) throw new BizException(40400, "User not found");
        // Soft delete via @TableLogic
        userMapper.deleteById(userId);
    }

    private static long num(Map<String, Object> row, String key) {
        if (row == null) return 0;
        Object v = row.get(key);
        if (v == null) return 0;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return 0; }
    }

    private boolean existsByUsername(String username) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0;
    }

    private boolean existsByEmail(String email) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, email)) > 0;
    }

    private AuthResponse issueToken(User user) {
        var token = jwt.issue(user.getId(), user.getUsername());
        return new AuthResponse(token, expireMinutes * 60_000L, UserProfile.of(user));
    }
}
