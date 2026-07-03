package com.example.api.model;

public record GameResult(
    String winner,
    String winningLine,
    boolean draw
) {
}