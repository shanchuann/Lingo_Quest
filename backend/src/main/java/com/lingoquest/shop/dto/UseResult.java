package com.lingoquest.shop.dto;

public record UseResult(
        Integer itemId,
        String effect,
        Integer remainingQuantity,
        String message
) {}
