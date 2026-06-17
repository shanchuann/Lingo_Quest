package com.lingoquest.shop.entity;

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
@TableName("user_inventory")
public class UserInventoryItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Long userId;
    @TableField("item_id")
    private Integer itemId;
    private Integer quantity;
    private Integer equipped;
    @TableField("acquired_at")
    private LocalDateTime acquiredAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
