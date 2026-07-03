# Jogo da Velha Online (Tic-Tac-Toe Multiplayer)

Uma aplicação web moderna de Jogo da Velha online e em tempo real. O projeto utiliza **Spring Boot** no backend com comunicação via **WebSockets** para atualizações instantâneas, armazenamento em cache para gerenciamento rápido de sessões de jogos, e um frontend interativo em **JavaScript Vanilla**. Tudo isso empacotado com **Docker** para facilitar o desenvolvimento e o deploy.

---

## Tecnologias Utilizadas

### Backend
* **Java 21** & **Spring Boot 3.x**
* **Spring WebSocket**: Para comunicação bidirecional e em tempo real (STOMP/SockJS).
* **Spring Boot Cache / Redis**: Para gerenciamento de estado dos jogos ativos em memória com alta performance.

### Frontend
* **HTML5 & CSS3**: Interface responsiva, limpa e moderna.
* **JavaScript (ES6+)**: Manipulação de DOM e integração com o cliente WebSocket (`@stomp/stompjs` ou `sockjs-client`).

### DevOps & Infraestrutura
* **Docker & Docker Compose**: Para containerização e execução simplificada de toda a infraestrutura (App + Redis).

---

## Arquitetura e Funcionamento

1. **Criação/Entrada no Jogo**: O Cliente A cria uma partida e recebe um código. O Cliente B insere o código para se conectar à mesma partida.
2. **Conexão WebSocket**: Ambos os clientes se inscrevem em um tópico específico do jogo (ex: `/topic/game/{gameId}`).
3. **Gerenciamento de Estado**: Cada jogada é enviada ao backend, validada pelas regras de negócio, salva no **Cache**, e o tabuleiro atualizado é transmitido simultaneamente para ambos os jogadores.

---

## Como Executar o Projeto

Você pode rodar o projeto localmente de duas formas: utilizando o Docker (recomendado) ou manualmente na sua máquina.

### Pré-requisitos
* [Docker](httpsin://www.docker.com/) e [Docker Compose](https://docs.docker.com/compose/) instalados.
* *Opcional (para rodar sem Docker):* JDK 21 e Maven instalados.

## Forma 1: Executando com Docker (Mais Rápido)

1. Clone o repositório:

```bash
git clone https://github.com/Bryan-014/jogo-da-velha-online
cd jogo-da-velha-online
```

2. Suba os containers com o Docker Compose:

```bash
docker compose up --build
```

3. Acesse a aplicação no seu navegador:

```text
http://localhost:8080
```

---

## Forma 2: Executando Manualmente (Desenvolvimento)

1. Inicie o Cache/Redis, caso o projeto utilize Redis standalone em vez do cache em memória do Spring:

```bash
docker run --name redis-tic-tac-toe -p 6379:6379 -d redis
```

2. Compile e rode o Backend:

```bash
mvn clean spring-boot:run
```

3. Abra o navegador em:

```text
http://localhost:8080
```

> Certifique-se de que os arquivos do frontend estão na pasta `src/main/resources/static`.

---

## Estrutura de Pastas Principal

```plaintext
├── .github/                 # Configurações de CI/CD (Opcional)
├── docker-compose.yml        # Orquestração dos containers (App + Redis)
├── Dockerfile                # Instruções de build da imagem Docker do Spring
├── pom.xml                   # Dependências do Maven
└── src/
    └── main/
        ├── java/com/exemplo/tictactoe/
        │   ├── config/       # Configurações de WebSocket e Cache
        │   ├── controller/   # Endpoints REST e Message Mappings (WebSocket)
        │   ├── model/        # Entidades do jogo (Game, Player, Board)
        │   └── service/      # Lógica de negócio e regras do Jogo da Velha
        └── resources/
            ├── application.properties # Configurações do Spring
            └── static/       # Frontend da aplicação (HTML, CSS, JS)
                ├── css/
                ├── js/
                └── index.html
```

---

## Endpoints & WebSockets

### HTTP REST

| Método | Endpoint       | Descrição                               |
| ------ | -------------- | --------------------------------------- |
| `POST` | `/game/create` | Cria um novo jogo e retorna o `gameId`. |

---

### WebSocket

| Tipo                | Endpoint/Destino       | Descrição                                            |
| ------------------- | ---------------------- | ---------------------------------------------------- |
| Endpoint de conexão | `/ws-tic-tac-toe`      | Endpoint utilizado para iniciar a conexão WebSocket. |
| Client → Server     | `/app/game/move`       | Envia a jogada atual para o servidor.                |
| Server → Client     | `/topic/game/{gameId}` | Recebe o estado atualizado do tabuleiro.             |


## Execução do front-end

O projeto precisa estar em servidor por causa da configuração de CORS da api, uma forma simples de executar o front-end para conectar sem problema com a API é utilizando a extenção `Live Server` do VsCode.  
