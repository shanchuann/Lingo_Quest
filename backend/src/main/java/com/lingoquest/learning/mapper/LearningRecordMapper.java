package com.lingoquest.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingoquest.learning.entity.LearningRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface LearningRecordMapper extends BaseMapper<LearningRecord> {

    @Select("""
            SELECT type AS questionType,
                   COUNT(*) AS total,
                   SUM(CASE WHEN lr.is_correct THEN 1 ELSE 0 END) AS correct
            FROM learning_record lr
            JOIN question q ON q.id = lr.question_id
            WHERE lr.user_id = #{userId} AND lr.created_at >= #{since}
            GROUP BY q.type
            """)
    List<Map<String, Object>> statsByType(@Param("userId") Long userId,
                                          @Param("since") LocalDateTime since);
}
