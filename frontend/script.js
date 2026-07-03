const API_BASE_URL = "http://localhost:8080/api";
const WS_URL = "http://localhost:8080/ws";

let game_board = [
    [null, null, null],
    [null, null, null],
    [null, null, null]
];

let hasConfigListners = false;
let gameActive = false;

let stompClient = null;
let currentGameCode = null;
let currentPlayerId = null;
let currentPlayerSymbol = null;

const getCellElement = (row, col) => {
    return document.getElementById(`cell-${row}-${col}`);
};

const setStatus = (message) => {
    document.getElementById("status-text").innerHTML = message;
};

const setGameInfo = () => {
    const gameInfo = document.getElementById("game-info");

    if (!currentGameCode || !currentPlayerSymbol) {
        gameInfo.innerHTML = "";
        return;
    }

    gameInfo.innerHTML = `
        Código: <strong>${currentGameCode}</strong> |
        Você é: <strong>${currentPlayerSymbol}</strong>
    `;
};

const request = async (url, options = {}) => {
    const response = await fetch(url, {
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        },
        ...options
    });

    const data = await response.json().catch(() => null);

    if (!response.ok) {
        const message = data?.message || "Erro ao processar a requisição.";
        throw new Error(message);
    }

    return data;
};

const createGame = async () => {
    try {
        const data = await request(`${API_BASE_URL}/games`, {
            method: "POST"
        });

        currentGameCode = data.code;
        currentPlayerId = data.playerId;
        currentPlayerSymbol = data.symbol;

        setGameInfo();
        connectWebSocket();
        renderGame(data.game);
    } catch (error) {
        alert(error.message);
    }
};

const joinGame = async () => {
    const code = document.getElementById("game-code-input").value.trim().toUpperCase();

    if (!code) {
        alert("Informe o código do jogo.");
        return;
    }

    try {
        const data = await request(`${API_BASE_URL}/games/join`, {
            method: "POST",
            body: JSON.stringify({ code })
        });

        currentGameCode = data.code;
        currentPlayerId = data.playerId;
        currentPlayerSymbol = data.symbol;

        setGameInfo();
        connectWebSocket();
        renderGame(data.game);
    } catch (error) {
        alert(error.message);
    }
};

const connectWebSocket = () => {
    if (stompClient) {
        stompClient.deactivate();
    }

    stompClient = new StompJs.Client({
        webSocketFactory: () => new SockJS(WS_URL),
        reconnectDelay: 5000,
        debug: () => {}
    });

    stompClient.onConnect = () => {
        stompClient.subscribe(`/topic/games/${currentGameCode}`, (message) => {
            const game = JSON.parse(message.body);
            renderGame(game);
        });
    };

    stompClient.activate();
};

const handleCellClick = async (row, col) => {
    if (!currentGameCode || !currentPlayerId) {
        alert("Crie um jogo ou entre em uma partida antes de jogar.");
        return;
    }

    if (!gameActive || game_board[row][col] !== null) {
        return;
    }

    try {
        await request(`${API_BASE_URL}/games/${currentGameCode}/moves`, {
            method: "POST",
            body: JSON.stringify({
                playerId: currentPlayerId,
                row,
                col
            })
        });
    } catch (error) {
        alert(error.message);
    }
};

const resetGame = async () => {
    if (!currentGameCode || !currentPlayerId) {
        alert("Nenhuma partida ativa.");
        return;
    }

    try {
        await request(`${API_BASE_URL}/games/${currentGameCode}/reset`, {
            method: "POST",
            body: JSON.stringify({
                playerId: currentPlayerId
            })
        });
    } catch (error) {
        alert(error.message);
    }
};

const renderGame = (game) => {
    game_board = game.board;
    gameActive = game.status === "IN_PROGRESS";

    document.getElementById("winning-line").className = "winning-line";

    for (let row = 0; row < 3; row++) {
        for (let col = 0; col < 3; col++) {
            const cell = getCellElement(row, col);
            const value = game.board[row][col];

            if (value === "X") {
                cell.innerHTML = `<div class="play x"></div>`;
            } else if (value === "O") {
                cell.innerHTML = `<div class="play o"></div>`;
            } else {
                cell.innerHTML = "";
            }
        }
    }

    if (game.status === "WAITING_PLAYER") {
        setStatus(`
            Código da partida: <strong>${game.code}</strong>.
            Aguardando outro jogador entrar.
        `);
        return;
    }

    if (game.status === "IN_PROGRESS") {
        const isMyTurn = game.currentTurn === currentPlayerSymbol;

        if (isMyTurn) {
            setStatus(`Sua vez: <strong>${game.currentTurn}</strong>`);
        } else {
            setStatus(`Aguardando jogada do Jogador: <strong>${game.currentTurn}</strong>`);
        }

        return;
    }

    if (game.status === "FINISHED") {
        gameActive = false;

        if (game.winner === "draw") {
            setStatus("Resultado: <strong>Deu Velha! Empate!</strong>");
        } else {
            setStatus(`Vitória: O Jogador <strong>${game.winner}</strong> venceu!`);

            if (game.winningLine) {
                document.getElementById("winning-line").className = `winning-line ${game.winningLine}`;
            }
        }
    }
};

const initGame = () => {
    game_board = [
        [null, null, null],
        [null, null, null],
        [null, null, null]
    ];

    gameActive = false;

    document.getElementById("winning-line").className = "winning-line";

    for (let row = 0; row < 3; row++) {
        for (let col = 0; col < 3; col++) {
            const cell = getCellElement(row, col);

            if (!hasConfigListners) {
                cell.addEventListener("click", () => handleCellClick(row, col));
            }

            cell.innerHTML = "";
        }
    }

    hasConfigListners = true;
};

document.getElementById("create-game-btn").addEventListener("click", createGame);
document.getElementById("join-game-btn").addEventListener("click", joinGame);
document.getElementById("reset-btn").addEventListener("click", resetGame);

initGame();