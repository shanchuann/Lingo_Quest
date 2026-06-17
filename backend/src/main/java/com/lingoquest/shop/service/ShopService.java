package com.lingoquest.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingoquest.achievement.mapper.UserAchievementMapper;
import com.lingoquest.achievement.service.AchievementService;
import com.lingoquest.common.exception.BizException;
import com.lingoquest.shop.dto.PurchaseResult;
import com.lingoquest.shop.dto.UseResult;
import com.lingoquest.shop.entity.ShopItem;
import com.lingoquest.shop.entity.UserInventoryItem;
import com.lingoquest.shop.mapper.ShopItemMapper;
import com.lingoquest.shop.mapper.UserInventoryMapper;
import com.lingoquest.user.entity.User;
import com.lingoquest.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopItemMapper itemMapper;
    private final UserInventoryMapper inventoryMapper;
    private final UserMapper userMapper;
    private final AchievementService achievementService;
    private final UserAchievementMapper userAchievementMapper;

    public List<ShopItem> catalog() {
        return itemMapper.selectList(
                new LambdaQueryWrapper<ShopItem>()
                        .eq(ShopItem::getEnabled, 1)
                        .orderByAsc(ShopItem::getSortOrder)
        );
    }

    private static final ObjectMapper OM = new ObjectMapper();

    public List<Map<String, Object>> inventoryOf(Long userId) {
        List<Map<String, Object>> raw = inventoryMapper.listForUser(userId);
        // MySQL JSON column returns as a String in a raw Map result — parse it so
        // the front-end can read payload.hue / payload.style directly.
        for (Map<String, Object> row : raw) {
            Object payload = row.get("payload");
            if (payload instanceof String s && !s.isEmpty()) {
                try {
                    row.put("payload", OM.readValue(s, new TypeReference<Map<String, Object>>() {}));
                } catch (Exception ignored) {
                    row.put("payload", null);
                }
            }
        }
        return raw;
    }

    @Transactional
    public PurchaseResult purchase(Long userId, Integer itemId) {
        ShopItem item = itemMapper.selectById(itemId);
        if (item == null || item.getEnabled() != 1) {
            throw new BizException(40400, "Item not available");
        }

        User user = userMapper.selectById(userId);
        if (user == null) throw new BizException(40400, "User not found");

        int coins = user.getCoins() == null ? 0 : user.getCoins();
        if (coins < item.getPrice()) {
            throw new BizException(40002, "金币不足:需要 " + item.getPrice() + ",当前 " + coins);
        }

        // Cosmetics can only be bought once
        UserInventoryItem existing = inventoryMapper.selectOne(
                new LambdaQueryWrapper<UserInventoryItem>()
                        .eq(UserInventoryItem::getUserId, userId)
                        .eq(UserInventoryItem::getItemId, itemId)
        );

        boolean isConsumable = item.getConsumable() != null && item.getConsumable() == 1;

        if (existing != null && !isConsumable) {
            throw new BizException(40009, "你已经拥有这件装饰道具了");
        }

        // Deduct coins
        user.setCoins(coins - item.getPrice());
        userMapper.updateById(user);

        int newQty;
        if (existing == null) {
            UserInventoryItem inv = new UserInventoryItem();
            inv.setUserId(userId);
            inv.setItemId(itemId);
            inv.setQuantity(1);
            // First cosmetic auto-equips and replaces any other cosmetic of the same effect.
            boolean autoEquip = !isConsumable;
            inv.setEquipped(autoEquip ? 1 : 0);
            inv.setAcquiredAt(LocalDateTime.now());
            inventoryMapper.insert(inv);
            newQty = 1;
            if (autoEquip) {
                // Un-equip other cosmetic of same effect
                List<UserInventoryItem> same = inventoryMapper.selectList(
                        new LambdaQueryWrapper<UserInventoryItem>()
                                .eq(UserInventoryItem::getUserId, userId)
                                .eq(UserInventoryItem::getEquipped, 1)
                                .ne(UserInventoryItem::getItemId, itemId));
                for (UserInventoryItem other : same) {
                    ShopItem otherItem = itemMapper.selectById(other.getItemId());
                    if (otherItem != null && item.getEffect().equals(otherItem.getEffect())) {
                        other.setEquipped(0);
                        inventoryMapper.updateById(other);
                    }
                }
            }
        } else {
            existing.setQuantity(existing.getQuantity() + 1);
            inventoryMapper.updateById(existing);
            newQty = existing.getQuantity();
        }

        return new PurchaseResult(itemId, item.getCode(), newQty, user.getCoins(),
                "已购入 · " + item.getName());
    }

    @Transactional
    public UseResult use(Long userId, Integer itemId) {
        ShopItem item = itemMapper.selectById(itemId);
        if (item == null) throw new BizException(40400, "Item not found");

        UserInventoryItem inv = inventoryMapper.selectOne(
                new LambdaQueryWrapper<UserInventoryItem>()
                        .eq(UserInventoryItem::getUserId, userId)
                        .eq(UserInventoryItem::getItemId, itemId)
        );
        if (inv == null || inv.getQuantity() <= 0) {
            throw new BizException(40004, "未拥有该道具");
        }

        boolean isConsumable = item.getConsumable() != null && item.getConsumable() == 1;
        if (!isConsumable) {
            throw new BizException(40008, "装饰道具请使用「装备」而非「使用」");
        }

        // Side effects per effect-type
        String effect = item.getEffect();
        String msg;
        switch (effect) {
            case "ACH_UNLOCK" -> {
                Object code = item.getPayload() == null ? null : item.getPayload().get("code");
                if (code == null) throw new BizException(50000, "Item payload missing code");
                // Idempotency guard — if the achievement was already unlocked,
                // refund the coins instead of silently consuming the card.
                if (userAchievementMapper.countByCode(userId, code.toString()) > 0) {
                    User user = userMapper.selectById(userId);
                    user.setCoins((user.getCoins() == null ? 0 : user.getCoins()) + item.getPrice());
                    userMapper.updateById(user);
                    throw new BizException(40010,
                            "你已解锁「" + code + "」徽章,无需重复使用 —— 金币已退回");
                }
                achievementService.unlockManually(userId, code.toString());
                msg = "已解锁成就 · " + code;
            }
            case "XP_DOUBLE" -> {
                // Flag the user; the next correct answer in GameService consumes it.
                User user = userMapper.selectById(userId);
                if (user.getXpDoublePending() != null && user.getXpDoublePending() == 1) {
                    throw new BizException(40011, "已有一张双倍经验待用,先答对一题再使用");
                }
                user.setXpDoublePending(1);
                userMapper.updateById(user);
                msg = "已激活双倍经验,答对下一题生效";
            }
            case "HINT", "RETRY", "SKIP" ->
                // These three are consumed in-game via dedicated GameController endpoints
                // (consumeOneByEffect). The generic /use is technically allowed but discouraged.
                msg = "请在棋盘中使用 · " + item.getName();
            default -> msg = "已使用";
        }

        // Decrement quantity
        inv.setQuantity(inv.getQuantity() - 1);
        if (inv.getQuantity() <= 0) {
            inventoryMapper.deleteById(inv.getId());
            return new UseResult(itemId, effect, 0, msg);
        } else {
            inventoryMapper.updateById(inv);
            return new UseResult(itemId, effect, inv.getQuantity(), msg);
        }
    }

    /** Consume one inventory item of the given effect.
     *  Used by GameController when player uses HINT / SKIP / RETRY in-game.
     *  Returns the ShopItem so the caller can read payload. */
    @Transactional
    public ShopItem consumeOneByEffect(Long userId, String effect) {
        // Find any inventory entry whose item.effect matches.
        List<Map<String, Object>> inv = inventoryMapper.listForUser(userId);
        for (Map<String, Object> row : inv) {
            if (effect.equals(row.get("effect"))) {
                int qty = ((Number) row.getOrDefault("quantity", 0)).intValue();
                if (qty <= 0) continue;
                Integer itemId = ((Number) row.get("itemId")).intValue();
                UserInventoryItem inv2 = inventoryMapper.selectOne(
                        new LambdaQueryWrapper<UserInventoryItem>()
                                .eq(UserInventoryItem::getUserId, userId)
                                .eq(UserInventoryItem::getItemId, itemId));
                if (inv2 == null) continue;
                inv2.setQuantity(inv2.getQuantity() - 1);
                if (inv2.getQuantity() <= 0) inventoryMapper.deleteById(inv2.getId());
                else                          inventoryMapper.updateById(inv2);
                return itemMapper.selectById(itemId);
            }
        }
        throw new BizException(40005, "你没有此类道具:" + effect);
    }

    @Transactional
    public void equip(Long userId, Integer itemId, boolean on) {
        ShopItem item = itemMapper.selectById(itemId);
        if (item == null) throw new BizException(40400, "Item not found");
        if (item.getConsumable() != null && item.getConsumable() == 1) {
            throw new BizException(40008, "消耗道具不能装备");
        }
        UserInventoryItem inv = inventoryMapper.selectOne(
                new LambdaQueryWrapper<UserInventoryItem>()
                        .eq(UserInventoryItem::getUserId, userId)
                        .eq(UserInventoryItem::getItemId, itemId)
        );
        if (inv == null) throw new BizException(40004, "未拥有该道具");

        if (on) {
            // un-equip other items of the same effect (so only one dice skin, one board theme...)
            List<UserInventoryItem> same = inventoryMapper.selectList(
                    new LambdaQueryWrapper<UserInventoryItem>()
                            .eq(UserInventoryItem::getUserId, userId)
                            .eq(UserInventoryItem::getEquipped, 1)
            );
            for (UserInventoryItem other : same) {
                ShopItem otherItem = itemMapper.selectById(other.getItemId());
                if (otherItem != null && otherItem.getEffect().equals(item.getEffect())) {
                    other.setEquipped(0);
                    inventoryMapper.updateById(other);
                }
            }
        }
        inv.setEquipped(on ? 1 : 0);
        inventoryMapper.updateById(inv);
    }
}
