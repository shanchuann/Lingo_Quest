package com.lingoquest.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingoquest.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("""
            SELECT
              (SELECT COUNT(*) FROM `game` WHERE user_id = #{userId} AND deleted = 0)                                     AS totalGames,
              (SELECT COUNT(*) FROM `game` WHERE user_id = #{userId} AND deleted = 0 AND status = 'FINISHED')             AS finishedGames,
              (SELECT COUNT(*) FROM learning_record WHERE user_id = #{userId})                                            AS totalAnswers,
              (SELECT COUNT(*) FROM learning_record WHERE user_id = #{userId} AND is_correct = 1)                         AS correctAnswers
            """)
    Map<String, Object> aggregateStats(@Param("userId") Long userId);

    @Select("""
            SELECT id, current_position AS currentPosition, round_num AS roundNum,
                   status, total_score AS totalScore,
                   start_time AS startTime, end_time AS endTime
              FROM `game`
             WHERE user_id = #{userId} AND deleted = 0
             ORDER BY start_time DESC
             LIMIT #{limit}
            """)
    List<Map<String, Object>> recentGames(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("""
            SELECT DATE(created_at) AS day,
                   SUM(is_correct = 1) AS correct,
                   COUNT(*) AS total
              FROM learning_record
             WHERE user_id = #{userId} AND created_at >= DATE_SUB(NOW(), INTERVAL #{days} DAY)
             GROUP BY DATE(created_at)
             ORDER BY day ASC
            """)
    List<Map<String, Object>> dailyAccuracy(@Param("userId") Long userId, @Param("days") int days);
}
