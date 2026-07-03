package com.example.api.model;

public class Game {

    private String code;
    private String[][] board;
    private String playerXId;
    private String playerOId;
    private String currentTurn;
    private GameStatus status;
    private String winner;
    private String winningLine;

    public Game(String code, String playerXId) {
        this.code = code;
        this.playerXId = playerXId;
        this.board = new String[3][3];
        this.currentTurn = "X";
        this.status = GameStatus.WAITING_PLAYER;
    }

    public String getCode() {
        return code;
    }

    public String[][] getBoard() {
        return board;
    }

    public String getPlayerXId() {
        return playerXId;
    }

    public String getPlayerOId() {
        return playerOId;
    }

    public void setPlayerOId(String playerOId) {
        this.playerOId = playerOId;
    }

    public String getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(String currentTurn) {
        this.currentTurn = currentTurn;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getWinningLine() {
        return winningLine;
    }

    public void setWinningLine(String winningLine) {
        this.winningLine = winningLine;
    }

    public String getSymbolByPlayerId(String playerId) {
        if (playerXId != null && playerXId.equals(playerId)) {
            return "X";
        }

        if (playerOId != null && playerOId.equals(playerId)) {
            return "O";
        }

        return null;
    }

    public void reset() {
        this.board = new String[3][3];
        this.currentTurn = "X";
        this.status = playerOId == null
            ? GameStatus.WAITING_PLAYER
            : GameStatus.IN_PROGRESS;
        this.winner = null;
        this.winningLine = null;
    }
}