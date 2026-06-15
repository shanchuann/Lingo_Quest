package com.lingoquest.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingoquest.common.exception.BizException;
import com.lingoquest.common.util.JwtTokenProvider;
import com.lingoquest.user.dto.AuthResponse;
import com.lingoquest.user.dto.LoginRequest;
import com.lingoquest.user.dto.RegisterRequest;
import com.lingoquest.user.dto.UserProfile;
import com.lingoquest.user.entity.User;
import com.lingoquest.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
