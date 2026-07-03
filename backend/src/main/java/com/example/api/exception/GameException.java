package com.example.api.exception;

import org.springframework.http.HttpStatus;

public class GameException extends RuntimeException {

    private final HttpStatus status;

    public GameException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}