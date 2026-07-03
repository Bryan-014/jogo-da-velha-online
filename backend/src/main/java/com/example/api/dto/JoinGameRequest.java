package com.example.api.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinGameRequest(
    @NotBlank
    String code
) {
}