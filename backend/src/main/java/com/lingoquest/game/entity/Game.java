package com.lingoquest.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("game")
public class Game {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer currentPosition;
    @TableField("previous_position")
    private Integer previousPosition;
    private Integer roundNum;
    private String status;
    private Integer totalScore;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @TableLogic
    private Integer deleted;
}
