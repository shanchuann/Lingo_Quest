package com.lingoquest.shop.dto;

public record PurchaseResult(
        Integer itemId,
        String code,
        Integer quantity,
        Integer remainingCoins,
        String message
) {}
