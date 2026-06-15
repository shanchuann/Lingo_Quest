package com.lingoquest.achievement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_achievement")
public class UserAchievement {
    private Long userId;
    private Integer achievementId;
    private LocalDateTime unlockedAt;
}
