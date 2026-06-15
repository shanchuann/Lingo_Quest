package com.lingoquest.user.controller;

import com.lingoquest.common.api.R;
import com.lingoquest.common.util.SecurityUtils;
import com.lingoquest.user.dto.AuthResponse;
import com.lingoquest.user.dto.LoginRequest;
import com.lingoquest.user.dto.RegisterRequest;
import com.lingoquest.user.dto.UserProfile;
import com.lingoquest.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}
