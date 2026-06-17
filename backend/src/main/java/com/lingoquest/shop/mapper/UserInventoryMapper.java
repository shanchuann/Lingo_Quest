package com.lingoquest.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingoquest.shop.entity.UserInventoryItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserInventoryMapper extends BaseMapper<UserInventoryItem> {

    @Select("""
            SELECT inv.id, inv.item_id AS itemId, inv.quantity, inv.equipped,
                   inv.acquired_at AS acquiredAt,
                   i.code, i.name, i.description, i.category, i.effect, i.price,
                   i.icon, i.payload, i.consumable
            FROM user_inventory inv
            JOIN shop_item i ON i.id = inv.item_id
            WHERE inv.user_id = #{userId}
            ORDER BY i.sort_order ASC, inv.acquired_at DESC
            """)
    List<Map<String, Object>> listForUser(@Param("userId") Long userId);
}
