package com.lingoquest.wordbook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingoquest.wordbook.entity.UserWordProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserWordProgressMapper extends BaseMapper<UserWordProgress> {

    @Select("""
            SELECT * FROM user_word_progress
             WHERE user_id = #{userId} AND word_id = #{wordId}
            """)
    UserWordProgress find(@Param("userId") Long userId, @Param("wordId") Long wordId);

    @Update("""
            INSERT INTO user_word_progress (user_id, word_id, state, reviewed_count, correct_count, updated_at)
            VALUES (#{userId}, #{wordId}, #{state}, 1, 0, NOW())
            ON DUPLICATE KEY UPDATE state = #{state},
                                    reviewed_count = reviewed_count + 1,
                                    updated_at = NOW()
            """)
    int upsert(@Param("userId") Long userId,
               @Param("wordId") Long wordId,
               @Param("state") String state);
}
