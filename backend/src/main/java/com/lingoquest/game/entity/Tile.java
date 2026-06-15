package com.lingoquest.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tile")
public class Tile {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer position;
    private String tileType;
    private String tileName;
    private Integer rewardExp;
    private Integer rewardCoin;
}
