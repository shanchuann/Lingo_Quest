package com.lingoquest.wordbook.entity;

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
@TableName("user_word_progress")
public class UserWordProgress {
    // Composite PK (user_id, word_id) — annotation on userId only suppresses
    // MyBatis-Plus warnings; uniqueness is enforced at the DB level.
    @TableId(type = IdType.NONE)
    @TableField("user_id")
    private Long userId;
    @TableField("word_id")
    private Long wordId;
    private String state;
    @TableField("reviewed_count")
    private Integer reviewedCount;
    @TableField("correct_count")
    private Integer correctCount;
    @TableField("next_review_at")
    private LocalDateTime nextReviewAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
