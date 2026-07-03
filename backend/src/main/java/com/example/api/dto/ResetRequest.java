package com.example.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetRequest(
    @NotBlank
    String playerId
) {
}