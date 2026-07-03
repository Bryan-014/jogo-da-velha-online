package com.example.api.service;

import com.example.api.dto.CreateGameResponse;
import com.example.api.dto.JoinGameResponse;
import com.example.api.exception.GameException;
import com.example.api.model.Game;
import com.example.api.model.GameResult;
import com.example.api.model.GameStatus;
import com.example.api.repository.GameCacheRepository;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.UUID;

@Service
public class GameService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_SIZE = 6;

    private final SecureRandom random = new SecureRandom();
    private final GameCacheRepository repository;
    private final SimpMessagingTemplate messagingTemplate;

    public GameService(
        GameCacheRepository repository,
        SimpMessagingTemplate messagingTemplate
    ) {
        this.repository = repository;
        this.messagingTemplate = messagingTemplate;
    }

    public CreateGameResponse createGame() {
        String code = generateUniqueCode();
        String playerId = UUID.randomUUID().toString();

        Game game = new Game(code, playerId);
        repository.save(game);

        return new CreateGameResponse(code, playerId, "X", game);
    }

    public JoinGameResponse joinGame(String code) {
        Game game = getGameOrThrow(code);

        synchronized (game) {
            if (game.getPlayerOId() != null) {
                throw new GameException("Este jogo já possui dois jogadores.", HttpStatus.CONFLICT);
            }

            String playerId = UUID.randomUUID().toString();
            game.setPlayerOId(playerId);
            game.setStatus(GameStatus.IN_PROGRESS);

            notifyGameUpdated(game);

            return new JoinGameResponse(game.getCode(), playerId, "O", game);
        }
    }

    public Game getGame(String code) {
        return getGameOrThrow(code);
    }

    public Game makeMove(String code, String playerId, int row, int col) {
        Game game = getGameOrThrow(code);

        synchronized (game) {
            validateMove(game, playerId, row, col);

            String symbol = game.getSymbolByPlayerId(playerId);
            game.getBoard()[row][col] = symbol;

            GameResult result = checkResult(game);

            if (result != null) {
                game.setStatus(GameStatus.FINISHED);

                if (result.draw()) {
                    game.setWinner("draw");
                    game.setWinningLine(null);
                } else {
                    game.setWinner(result.winner());
                    game.setWinningLine(result.winningLine());
                }
            } else {
                game.setCurrentTurn(symbol.equals("X") ? "O" : "X");
            }

            notifyGameUpdated(game);

            return game;
        }
    }

    public Game resetGame(String code, String playerId) {
        Game game = getGameOrThrow(code);

        synchronized (game) {
            String symbol = game.getSymbolByPlayerId(playerId);

            if (symbol == null) {
                throw new GameException("Jogador não pertence a este jogo.", HttpStatus.FORBIDDEN);
            }

            game.reset();

            notifyGameUpdated(game);

            return game;
        }
    }

    private void validateMove(Game game, String playerId, int row, int col) {
        if (game.getStatus() == GameStatus.WAITING_PLAYER) {
            throw new GameException("Aguardando outro jogador entrar.", HttpStatus.CONFLICT);
        }

        if (game.getStatus() == GameStatus.FINISHED) {
            throw new GameException("Este jogo já foi finalizado.", HttpStatus.CONFLICT);
        }

        String symbol = game.getSymbolByPlayerId(playerId);

        if (symbol == null) {
            throw new GameException("Jogador não pertence a este jogo.", HttpStatus.FORBIDDEN);
        }

        if (!symbol.equals(game.getCurrentTurn())) {
            throw new GameException("Ainda não é a vez deste jogador.", HttpStatus.CONFLICT);
        }

        if (game.getBoard()[row][col] != null) {
            throw new GameException("Esta posição já foi ocupada.", HttpStatus.CONFLICT);
        }
    }

    private GameResult checkResult(Game game) {
        String[][] board = game.getBoard();

        for (int row = 0; row < 3; row++) {
            if (
                board[row][0] != null &&
                board[row][0].equals(board[row][1]) &&
                board[row][1].equals(board[row][2])
            ) {
                return new GameResult(board[row][0], "row-" + row, false);
            }
        }

        for (int col = 0; col < 3; col++) {
            if (
                board[0][col] != null &&
                board[0][col].equals(board[1][col]) &&
                board[1][col].equals(board[2][col])
            ) {
                return new GameResult(board[0][col], "col-" + col, false);
            }
        }

        if (
            board[0][0] != null &&
            board[0][0].equals(board[1][1]) &&
            board[1][1].equals(board[2][2])
        ) {
            return new GameResult(board[0][0], "diag-p", false);
        }

        if (
            board[0][2] != null &&
            board[0][2].equals(board[1][1]) &&
            board[1][1].equals(board[2][0])
        ) {
            return new GameResult(board[0][2], "diag-s", false);
        }

        boolean draw = true;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (board[row][col] == null) {
                    draw = false;
                    break;
                }
            }
        }

        if (draw) {
            return new GameResult("draw", null, true);
        }

        return null;
    }

    private Game getGameOrThrow(String code) {
        return repository
            .findByCode(code.toUpperCase())
            .orElseThrow(() -> new GameException("Jogo não encontrado.", HttpStatus.NOT_FOUND));
    }

    private void notifyGameUpdated(Game game) {
        messagingTemplate.convertAndSend(
            "/topic/games/" + game.getCode(),
            game
        );
    }

    private String generateUniqueCode() {
        String code;

        do {
            code = generateCode();
        } while (repository.existsByCode(code));

        return code;
    }

    private String generateCode() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < CODE_SIZE; i++) {
            int index = random.nextInt(CODE_CHARS.length());
            builder.append(CODE_CHARS.charAt(index));
        }

        return builder.toString();
    }
}