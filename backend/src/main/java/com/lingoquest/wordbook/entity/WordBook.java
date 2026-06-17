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
@TableName("word_book")
public class WordBook {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String code;
    private String name;
    private String description;
    private String level;
    @TableField("cover_color")
    private String coverColor;
    @TableField("total_words")
    private Integer totalWords;
    @TableField("sort_order")
    private Integer sortOrder;
    private Integer enabled;
}
