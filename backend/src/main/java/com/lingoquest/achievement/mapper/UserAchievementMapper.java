package com.lingoquest.achievement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingoquest.achievement.entity.UserAchievement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserAchievementMapper extends BaseMapper<UserAchievement> {

    @Select("""
            SELECT a.id, a.code, a.title, a.description, a.icon,
                   a.reward_exp AS rewardExp, a.reward_coin AS rewardCoin,
                   ua.unlocked_at AS unlockedAt
            FROM achievement a
            JOIN user_achievement ua ON ua.achievement_id = a.id
            WHERE ua.user_id = #{userId}
            ORDER BY ua.unlocked_at DESC
            """)
    List<Map<String, Object>> listUnlocked(@Param("userId") Long userId);
}
