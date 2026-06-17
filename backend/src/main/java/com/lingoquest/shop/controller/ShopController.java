package com.lingoquest.shop.controller;

import com.lingoquest.common.api.R;
import com.lingoquest.common.util.SecurityUtils;
import com.lingoquest.shop.dto.PurchaseResult;
import com.lingoquest.shop.dto.UseResult;
import com.lingoquest.shop.entity.ShopItem;
import com.lingoquest.shop.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "Shop")
@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @Operation(summary = "Browse shop catalog")
    @GetMapping("/items")
    public R<List<ShopItem>> items() {
        return R.ok(shopService.catalog());
    }

    @Operation(summary = "My inventory")
    @GetMapping("/inventory")
    public R<List<Map<String, Object>>> inventory() {
        return R.ok(shopService.inventoryOf(SecurityUtils.currentUserId()));
    }

    @Operation(summary = "Purchase an item")
    @PostMapping("/purchase/{itemId}")
    public R<PurchaseResult> purchase(@PathVariable Integer itemId) {
        return R.ok(shopService.purchase(SecurityUtils.currentUserId(), itemId));
    }

    @Operation(summary = "Use a consumable item")
    @PostMapping("/inventory/{itemId}/use")
    public R<UseResult> use(@PathVariable Integer itemId) {
        return R.ok(shopService.use(SecurityUtils.currentUserId(), itemId));
    }

    @Operation(summary = "Equip / un-equip a cosmetic")
    @PostMapping("/inventory/{itemId}/equip")
    public R<Void> equip(@PathVariable Integer itemId, @RequestParam(defaultValue = "true") boolean on) {
        shopService.equip(SecurityUtils.currentUserId(), itemId, on);
        return R.ok();
    }
}
