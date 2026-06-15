package com.lingoquest.achievement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("achievement")
public class Achievement {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String code;
    private String title;
    private String description;
    private String icon;
    private Integer rewardExp;
    private Integer rewardCoin;
}
