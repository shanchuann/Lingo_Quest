package com.lingoquest.shop.service;

import com.lingoquest.achievement.mapper.UserAchievementMapper;
import com.lingoquest.achievement.service.AchievementService;
import com.lingoquest.common.exception.BizException;
import com.lingoquest.shop.entity.ShopItem;
import com.lingoquest.shop.entity.UserInventoryItem;
import com.lingoquest.shop.mapper.ShopItemMapper;
import com.lingoquest.shop.mapper.UserInventoryMapper;
import com.lingoquest.user.entity.User;
import com.lingoquest.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock ShopItemMapper itemMapper;
    @Mock UserInventoryMapper inventoryMapper;
    @Mock UserMapper userMapper;
    @Mock AchievementService achievementService;
    @Mock UserAchievementMapper userAchievementMapper;

    @InjectMocks ShopService shopService;

    private User user;
    private ShopItem cosmetic;
    private ShopItem accelerator;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setCoins(100);

        cosmetic = new ShopItem();
        cosmetic.setId(10);
        cosmetic.setCode("DICE_GOLD");
        cosmetic.setName("镀金骰子");
        cosmetic.setCategory("COSMETIC");
        cosmetic.setEffect("DICE_SKIN");
        cosmetic.setPrice(120);
        cosmetic.setConsumable(0);
        cosmetic.setEnabled(1);

        accelerator = new ShopItem();
        accelerator.setId(20);
        accelerator.setCode("ACH_VOCAB");
        accelerator.setName("词海速记");
        accelerator.setCategory("ACCELERATOR");
        accelerator.setEffect("ACH_UNLOCK");
        accelerator.setPrice(300);
        accelerator.setConsumable(1);
        accelerator.setEnabled(1);
        accelerator.setPayload(Map.of("code", "VOCAB_MASTER"));
    }

    @Test
    @DisplayName("purchase rejects when coins < price")
    void rejectsWhenInsufficientCoins() {
        user.setCoins(50);                // less than cosmetic.price=120
        when(itemMapper.selectById(10)).thenReturn(cosmetic);
        when(userMapper.selectById(1L)).thenReturn(user);

        assertThatThrownBy(() -> shopService.purchase(1L, 10))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("金币不足");

        verify(userMapper, never()).updateById(any(User.class));
        verify(inventoryMapper, never()).insert(any(UserInventoryItem.class));
    }

    @Test
    @DisplayName("cosmetic cannot be purchased twice")
    void rejectsDuplicateCosmetic() {
        user.setCoins(500);                    // enough to pay
        when(itemMapper.selectById(10)).thenReturn(cosmetic);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(inventoryMapper.selectOne(any())).thenReturn(new UserInventoryItem());

        assertThatThrownBy(() -> shopService.purchase(1L, 10))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("已经拥有");
    }

    @Test
    @DisplayName("ACH_UNLOCK refunds coins when achievement already unlocked")
    void acceleratorRefundsWhenAlreadyUnlocked() {
        // First, the user owns the accelerator card.
        UserInventoryItem ownedCard = new UserInventoryItem();
        ownedCard.setId(99L);
        ownedCard.setUserId(1L);
        ownedCard.setItemId(20);
        ownedCard.setQuantity(1);

        user.setCoins(50);                                 // we'll verify refund += price
        when(itemMapper.selectById(20)).thenReturn(accelerator);
        when(inventoryMapper.selectOne(any())).thenReturn(ownedCard);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(userAchievementMapper.countByCode(1L, "VOCAB_MASTER")).thenReturn(1); // already unlocked

        assertThatThrownBy(() -> shopService.use(1L, 20))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("无需重复使用");

        // user.coins should have been refunded (+= accelerator.price)
        ArgumentCaptor<User> updated = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(updated.capture());
        assertThat(updated.getValue().getCoins()).isEqualTo(50 + 300);

        // Card was NOT consumed
        verify(achievementService, never()).unlockManually(anyLong(), anyString());
    }

    @Test
    @DisplayName("ACH_UNLOCK actually unlocks when achievement not yet earned")
    void acceleratorUnlocksWhenAvailable() {
        UserInventoryItem ownedCard = new UserInventoryItem();
        ownedCard.setId(99L);
        ownedCard.setUserId(1L);
        ownedCard.setItemId(20);
        ownedCard.setQuantity(1);

        when(itemMapper.selectById(20)).thenReturn(accelerator);
        when(inventoryMapper.selectOne(any())).thenReturn(ownedCard);
        when(userAchievementMapper.countByCode(1L, "VOCAB_MASTER")).thenReturn(0);

        var result = shopService.use(1L, 20);

        verify(achievementService, times(1)).unlockManually(1L, "VOCAB_MASTER");
        // Quantity hit zero → deleted
        verify(inventoryMapper).deleteById(99L);
        assertThat(result.remainingQuantity()).isZero();
    }

    @Test
    @DisplayName("XP_DOUBLE 'use' sets the user's pending flag without immediate effect on coins")
    void xpDoubleFlagsTheUser() {
        ShopItem xp2x = new ShopItem();
        xp2x.setId(30);
        xp2x.setCode("XP_DOUBLE");
        xp2x.setCategory("FUNCTIONAL");
        xp2x.setEffect("XP_DOUBLE");
        xp2x.setPrice(80);
        xp2x.setConsumable(1);

        UserInventoryItem ownedCard = new UserInventoryItem();
        ownedCard.setId(7L);
        ownedCard.setUserId(1L);
        ownedCard.setItemId(30);
        ownedCard.setQuantity(2);

        when(itemMapper.selectById(30)).thenReturn(xp2x);
        when(inventoryMapper.selectOne(any())).thenReturn(ownedCard);
        when(userMapper.selectById(1L)).thenReturn(user);

        var result = shopService.use(1L, 30);

        ArgumentCaptor<User> updated = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(updated.capture());
        assertThat(updated.getValue().getXpDoublePending()).isEqualTo(1);
        assertThat(result.remainingQuantity()).isEqualTo(1);     // 2 → 1
    }

    @Test
    @DisplayName("consumeOneByEffect finds & decrements the inventory item of given effect")
    void consumeOneByEffectDecrements() {
        UserInventoryItem inv = new UserInventoryItem();
        inv.setId(42L);
        inv.setUserId(1L);
        inv.setItemId(50);
        inv.setQuantity(3);

        when(inventoryMapper.listForUser(1L)).thenReturn(List.of(Map.of(
                "id", 42L,
                "itemId", 50,
                "quantity", 3,
                "effect", "HINT"
        )));
        when(inventoryMapper.selectOne(any())).thenReturn(inv);
        ShopItem hint = new ShopItem();
        hint.setId(50);
        hint.setEffect("HINT");
        when(itemMapper.selectById(50)).thenReturn(hint);

        var consumed = shopService.consumeOneByEffect(1L, "HINT");

        assertThat(consumed.getId()).isEqualTo(50);
        ArgumentCaptor<UserInventoryItem> cap = ArgumentCaptor.forClass(UserInventoryItem.class);
        verify(inventoryMapper).updateById(cap.capture());
        assertThat(cap.getValue().getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("consumeOneByEffect throws when no item of that effect")
    void consumeOneByEffectThrowsWhenMissing() {
        when(inventoryMapper.listForUser(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> shopService.consumeOneByEffect(1L, "HINT"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("你没有此类道具");
    }
}
