package com.lingoquest.game.dto;

public record TileView(
        int position,
        String tileType,
        String tileName,
        int rewardExp,
        int rewardCoin
) {}
