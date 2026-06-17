package com.lingoquest.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "shop_item", autoResultMap = true)
public class ShopItem {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String code;
    private String name;
    private String description;
    private String category;          // FUNCTIONAL | COSMETIC | ACCELERATOR
    private String effect;            // HINT | RETRY | SKIP | XP_DOUBLE | DICE_SKIN | BOARD_THEME | AVATAR_FRAME | ACH_UNLOCK
    private Integer price;
    private String icon;

    @TableField(value = "payload", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> payload;

    private Integer consumable;       // 1 / 0
    @TableField("sort_order")
    private Integer sortOrder;
    private Integer enabled;
}
