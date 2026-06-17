package com.lingoquest.wordbook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingoquest.wordbook.entity.Word;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface WordMapper extends BaseMapper<Word> {

    @Select("""
            SELECT w.id, w.word, w.phonetic, w.part_of_speech AS partOfSpeech,
                   w.meaning_zh AS meaningZh, w.meaning_en AS meaningEn,
                   w.example_en AS exampleEn, w.example_zh AS exampleZh,
                   w.difficulty, w.sort_order AS sortOrder,
                   COALESCE(p.state, 'NEW') AS state
              FROM word w
              LEFT JOIN user_word_progress p
                ON p.word_id = w.id AND p.user_id = #{userId}
             WHERE w.book_id = #{bookId}
               AND (#{state} IS NULL OR COALESCE(p.state, 'NEW') = #{state})
             ORDER BY w.sort_order ASC, w.id ASC
            """)
    List<Map<String, Object>> listForUser(@Param("userId") Long userId,
                                          @Param("bookId") Integer bookId,
                                          @Param("state") String state);

    @Select("""
            SELECT w.* FROM word w
             WHERE w.book_id = #{bookId}
             ORDER BY RAND()
             LIMIT 1
            """)
    Word pickRandom(@Param("bookId") Integer bookId);
}
