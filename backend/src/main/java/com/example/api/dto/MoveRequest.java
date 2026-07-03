package com.example.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record MoveRequest(
    @NotBlank
    String playerId,

    @Min(0)
    @Max(2)
    int row,

    @Min(0)
    @Max(2)
    int col
) {
}