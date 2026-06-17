package com.lingoquest.user.dto;

import com.lingoquest.user.entity.User;

public record UserProfile(
        Long id,
        String username,
        String email,
        String avatar,
        Integer level,
        Integer experience,
        Integer coins,
        Integer currentBookId
) {
    public static UserProfile of(User u) {
        return new UserProfile(u.getId(), u.getUsername(), u.getEmail(), u.getAvatar(),
                u.getLevel(), u.getExperience(), u.getCoins(), u.getCurrentBookId());
    }
}
