package com.lingoquest.wordbook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("word")
public class Word {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("book_id")
    private Integer bookId;
    private String word;
    private String phonetic;
    @TableField("part_of_speech")
    private String partOfSpeech;
    @TableField("meaning_zh")
    private String meaningZh;
    @TableField("meaning_en")
    private String meaningEn;
    @TableField("example_en")
    private String exampleEn;
    @TableField("example_zh")
    private String exampleZh;
    private Integer difficulty;
    @TableField("sort_order")
    private Integer sortOrder;
}
