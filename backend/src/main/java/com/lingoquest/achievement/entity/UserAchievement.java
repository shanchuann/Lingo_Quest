package com.lingoquest.achievement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
    // Composite PK (user_id, achievement_id) at the DB level.
    // Annotating one column with IdType.NONE silences MyBatis-Plus warnings;
    // selectById is unused for this join entity.
    @TableId(type = IdType.NONE)
    @TableField("user_id")
    private Long userId;

    @TableField("achievement_id")
    private Integer achievementId;

    @TableField("unlocked_at")
    private LocalDateTime unlockedAt;
}
