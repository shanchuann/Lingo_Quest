package com.lingoquest.wordbook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingoquest.wordbook.entity.WordBook;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface WordBookMapper extends BaseMapper<WordBook> {

    @Select("""
            SELECT wb.id, wb.code, wb.name, wb.description, wb.level,
                   wb.cover_color AS coverColor, wb.total_words AS totalWords,
                   wb.sort_order AS sortOrder,
                   COALESCE(p.mastered, 0) AS masteredCount,
                   COALESCE(p.learning, 0) AS learningCount
            FROM word_book wb
            LEFT JOIN (
                SELECT w.book_id,
                       SUM(CASE WHEN p.state = 'MASTERED' THEN 1 ELSE 0 END) AS mastered,
                       SUM(CASE WHEN p.state = 'LEARNING' THEN 1 ELSE 0 END) AS learning
                  FROM user_word_progress p
                  JOIN word w ON w.id = p.word_id
                 WHERE p.user_id = #{userId}
                 GROUP BY w.book_id
            ) p ON p.book_id = wb.id
            WHERE wb.enabled = 1
            ORDER BY wb.sort_order ASC
            """)
    List<Map<String, Object>> listWithProgress(@Param("userId") Long userId);
}
