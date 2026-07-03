package com.example.api.controller;

import com.example.api.dto.CreateGameResponse;
import com.example.api.dto.JoinGameRequest;
import com.example.api.dto.JoinGameResponse;
import com.example.api.dto.MoveRequest;
import com.example.api.dto.ResetRequest;
import com.example.api.model.Game;
import com.example.api.service.GameService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService service;

    public GameController(GameService service) {
        this.service = service;
    }

    @PostMapping
    public CreateGameResponse createGame() {
        return service.createGame();
    }

    @PostMapping("/join")
    public JoinGameResponse joinGame(@RequestBody @Valid JoinGameRequest request) {
        return service.joinGame(request.code());
    }

    @GetMapping("/{code}")
    public Game getGame(@PathVariable String code) {
        return service.getGame(code);
    }

    @PostMapping("/{code}/moves")
    public Game makeMove(
        @PathVariable String code,
        @RequestBody @Valid MoveRequest request
    ) {
        return service.makeMove(
            code,
            request.playerId(),
            request.row(),
            request.col()
        );
    }

    @PostMapping("/{code}/reset")
    public Game resetGame(
        @PathVariable String code,
        @RequestBody @Valid ResetRequest request
    ) {
        return service.resetGame(code, request.playerId());
    }
}