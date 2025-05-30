package model.game

import model.toDto
import websoket.state.GameCommand
import websoket.state.GameEvent
import websoket.state.GameState
import websoket.state.PlayerScoreOutOfBounds
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class BlackjackGame {
    private val players = ConcurrentHashMap<String, Player>()
    private val deck = Deck()
    private var currentPlayerIndex: Int = 0
    var gameInProgress = false
    val isGameOver: Boolean
        get() = gameInProgress && players.values.all { it.isStanding || it.score > 21 }
    val playersCount: Int
        get() = players.size

    val playerIds: List<String>
        get() = players.keys.toList()

    fun addPlayer(name: String): Player {
        val playerUUID = UUID.randomUUID().toString()
        val player = Player(
            id = playerUUID,
            name = name,
        )
        players[playerUUID] = player
        return player
    }

    fun dealInitialCards() {
        repeat(2) {
            players.forEach { (_, player) ->
                dealCards(player)
            }
        }
    }

    fun dealCards(player: Player): Card {
        return player.addCard(deck.getRandomCard())
    }

    fun startGame(): GameState {
        if (players.size != 2) {
            throw IllegalArgumentException("Необходимо ровно 2 игрока для начала игры")
        }
        gameInProgress = true
        // Случайным образом выбираем игрока, который начинает
        currentPlayerIndex = (0..1).random()
        dealInitialCards()
        return getGameState()
    }

    fun hit(playerId: String): Pair<Card, Boolean> {
        val player = players[playerId] ?: throw IllegalArgumentException("Игрок не найден")
        val playersList = players.values.toList()

        // Проверяем, что это ход текущего игрока
        if (playersList[currentPlayerIndex].id != playerId) {
            throw IllegalStateException("Сейчас ход другого игрока")
        }

        if (player.isStanding || player.score > 21) {
            throw PlayerScoreOutOfBounds(playerId, player.score)
        }

        val card = dealCards(player)
        var busted = false

        // Если у игрока перебор, автоматически передаем ход
        if (player.score > 21) {
            player.isStanding = true
            busted = true
            nextPlayer()
            // Проверяем, не закончилась ли игра
            if (isGameOver) {
                gameInProgress = false
            }
        }

        return Pair(card, busted)
    }

    fun stand(playerId: String) {
        val player = players[playerId] ?: throw IllegalArgumentException("Игрок не найден")
        val playersList = players.values.toList()

        // Проверяем, что это ход текущего игрока
        if (playersList[currentPlayerIndex].id != playerId) {
            throw IllegalStateException("Сейчас ход другого игрока")
        }

        player.isStanding = true
        nextPlayer()

        // Проверяем, не закончилась ли игра
        if (isGameOver) {
            gameInProgress = false
        }
    }

    private fun nextPlayer() {
        val playersList = players.values.toList()
        // Переключаемся на другого игрока
        currentPlayerIndex = (currentPlayerIndex + 1) % playersList.size

        // Если следующий игрок уже закончил игру (стоит или перебор), игра завершается
        val nextPlayer = playersList[currentPlayerIndex]
        if (nextPlayer.isStanding || nextPlayer.score > 21) {
            gameInProgress = false
        }
    }

    fun getWinner(): String? {
        val validPlayers = players.values.filter { it.score <= 21 }

        return when (validPlayers.size) {
            0 -> {
                players.playerIdWithMinScoreOrNull()
            }

            1 -> {
                validPlayers.first().id
            }

            else -> {
                players.playerIdWithMaxScoreOrNull()
            }
        }
    }

    fun Map<String, Player>.playerIdWithMaxScoreOrNull(): String? {
        val scores = this.values.map { it.score }
        return if (scores.distinct().size > 1) {
            this.maxByOrNull { it.value.score }?.key
        } else null
    }

    fun Map<String, Player>.playerIdWithMinScoreOrNull(): String? {
        val scores = this.values.map { it.score }
        return if (scores.distinct().size > 1) {
            this.minByOrNull { it.value.score }?.key
        } else null
    }

    fun getGameState(): GameState {
        val playersList = players.values.toList()
        val currentPlayerId = if (gameInProgress && playersList.isNotEmpty() && currentPlayerIndex < players.size) {
            playersList[currentPlayerIndex].id
        } else {
            ""
        }

        val gameOver = !gameInProgress && (isGameOver || playersList.size == 2)

        val message = when {
            gameOver -> ""
            gameInProgress -> {
                val currentPlayer = playersList.getOrNull(currentPlayerIndex)
                if (currentPlayer != null) {
                    "Ход игрока: ${currentPlayer.name} (${currentPlayer.score} очков)"
                } else {
                    "Игра продолжается"
                }
            }

            else -> "Ожидание игроков..."
        }

        return GameState(
            players = playersList,
            currentPlayerId = currentPlayerId,
            gameOver = gameOver,
            message = message,
        )
    }

    fun processCommand(command: GameCommand): GameEvent {
        return when (command) {
            is GameCommand.Join -> {
                // Join уже обработан в GameWebSocket
                GameEvent.Info("Игрок присоединился")
            }

            is GameCommand.StartGame -> {
                if (gameInProgress) {
                    GameEvent.Error("Игра уже идёт")
                } else {
                    try {
                        val gameState = startGame()
                        GameEvent.GameStarted(gameState)
                    } catch (e: Exception) {
                        GameEvent.Error(e.message ?: "Неизвестная ошибка")
                    }
                }
            }

            is GameCommand.Hit -> {
                if (!gameInProgress) {
                    GameEvent.Error("Игра ещё не началась")
                } else {
                    try {
                        val card = hit(command.playerId)
                        if (players[command.playerId]?.isStanding == true && players.values.all { it.isStanding }) {
                            gameInProgress = false
                            GameEvent.GameOver(getWinner(), getGameState())
                        } else {
                            GameEvent.CardDealt(command.playerId, card.first.toDto())
                        }
                    } catch (e: Exception) {
                        GameEvent.Error(e.message ?: "Неизвестная ошибка")
                    }
                }
            }

            is GameCommand.Stand -> {
                if (!gameInProgress) {
                    GameEvent.Error("Игра ещё не началась")
                } else {
                    try {
                        stand(command.playerId)
                        if (players.values.all { it.isStanding }) {
                            gameInProgress = false
                            GameEvent.GameOver(getWinner(), getGameState())
                        } else {
                            GameEvent.PlayerStood(command.playerId)
                        }
                    } catch (e: Exception) {
                        GameEvent.Error(e.message ?: "Неизвестная ошибка")
                    }
                }
            }
        }
    }

    fun getOpponentId(playerId: String): String? {
        return players.values.find { it.id != playerId }?.id
    }
}