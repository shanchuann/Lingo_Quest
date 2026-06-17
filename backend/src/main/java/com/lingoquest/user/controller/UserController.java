package com.lingoquest.user.controller;

import com.lingoquest.common.api.R;
import com.lingoquest.common.util.SecurityUtils;
import com.lingoquest.user.dto.AuthResponse;
import com.lingoquest.user.dto.LoginRequest;
import com.lingoquest.user.dto.PasswordChangeRequest;
import com.lingoquest.user.dto.ProfileUpdateRequest;
import com.lingoquest.user.dto.RegisterRequest;
import com.lingoquest.user.dto.UserProfile;
import com.lingoquest.user.dto.UserStats;
import com.lingoquest.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth & User")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "Register a new player")
    @PostMapping("/auth/register")
    public R<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return R.ok(userService.register(req));
    }

    @Operation(summary = "Login with username + password")
    @PostMapping("/auth/login")
    public R<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return R.ok(userService.login(req));
    }

    @Operation(summary = "Get the current user's profile")
    @GetMapping("/users/me")
    public R<UserProfile> me() {
        return R.ok(userService.me(SecurityUtils.currentUserId()));
    }

    @Operation(summary = "Update the current user's email / avatar")
    @PutMapping("/users/me")
    public R<UserProfile> updateMe(@Valid @RequestBody ProfileUpdateRequest req) {
        return R.ok(userService.updateProfile(SecurityUtils.currentUserId(), req));
    }

    @Operation(summary = "Change password")
    @PutMapping("/users/me/password")
    public R<Void> changePassword(@Valid @RequestBody PasswordChangeRequest req) {
        userService.changePassword(SecurityUtils.currentUserId(), req);
        return R.ok();
    }

    @Operation(summary = "Aggregate gameplay stats for the current user")
    @GetMapping("/users/me/stats")
    public R<UserStats> stats() {
        return R.ok(userService.stats(SecurityUtils.currentUserId()));
    }

    @Operation(summary = "Soft-delete (deactivate) the current account")
    @DeleteMapping("/users/me")
    public R<Void> deleteMe() {
        userService.deleteSelf(SecurityUtils.currentUserId());
        return R.ok();
    }

    @Operation(summary = "Recent games for the current user")
    @GetMapping("/users/me/games")
    public R<java.util.List<java.util.Map<String, Object>>> games(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int limit) {
        return R.ok(userService.recentGames(SecurityUtils.currentUserId(), Math.min(Math.max(limit, 1), 50)));
    }

    @Operation(summary = "Daily accuracy trend (last N days, default 14)")
    @GetMapping("/users/me/stats/trend")
    public R<java.util.List<java.util.Map<String, Object>>> trend(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "14") int days) {
        return R.ok(userService.dailyAccuracy(SecurityUtils.currentUserId(), Math.min(Math.max(days, 1), 60)));
    }
}
