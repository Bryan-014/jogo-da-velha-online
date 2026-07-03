package com.example.api.dto;

import com.example.api.model.Game;

public record CreateGameResponse(
    String code,
    String playerId,
    String symbol,
    Game game
) {
}