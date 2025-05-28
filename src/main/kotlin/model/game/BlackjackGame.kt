package model.game

import websoket.state.GameCommand
import websoket.state.GameEvent
import websoket.state.GameState
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class BlackjackGame {
    private val players = ConcurrentHashMap<String, Player>()
    private val deck = Deck()
    private var currentPlayerIndex: Int = 0
    var gameInProgress = false
    val isGameOver: Boolean
        get() = !gameInProgress && players.values.all { it.isStanding }
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
        currentPlayerIndex = 0
        dealInitialCards()
        return getGameState()
    }

    fun hit(playerId: String): Card {
        val player = players[playerId] ?: throw IllegalArgumentException("Игрок не найден")

        if (player.isStanding) {
            throw IllegalStateException("Игроку достаточно карт")
        }

        val card = dealCards(player)

        if (player.score > 21) {
            player.isStanding = true
            nextPlayer()
        }

        return card
    }

    fun stand(playerId: String) {
        val player = players[playerId] ?: throw IllegalArgumentException("Игрок не найден")
        player.isStanding = true
        nextPlayer()
    }

    private fun nextPlayer() {
        val playersList = players.values.toList()
        var nextIndex = (currentPlayerIndex + 1) % playersList.size
        while (nextIndex != currentPlayerIndex) {
            if (!playersList[nextIndex].isStanding) {
                currentPlayerIndex = nextIndex
                return
            }
            nextIndex = (nextIndex + 1) % playersList.size
        }
    }

    fun getWinner(): Player? {
        val validPlayers = players.filter { it.value.score <= 21 }
        if (validPlayers.size == 1)
            return validPlayers.values.first()
        else if (validPlayers.size == 2) {
            return validPlayers.values.maxByOrNull { it.score }
        }

        return players.values.minByOrNull { it.score }
    }

    fun getGameState(): GameState {
        val playersList = players.values.toList()
        val currentPlayerId = if (playersList.isNotEmpty() && currentPlayerIndex < players.size) {
            playersList[currentPlayerIndex].id
        } else {
            ""
        }

        val winner = if (!gameInProgress && playersList.all { it.isStanding }) getWinner() else null
        val gameOver = !gameInProgress

        val message = when {
            gameOver && winner != null -> "Игра завершена! Победитель: ${winner.name} с ${winner.score} очками!"
            gameOver -> "Игра завершена! Ничья!"
            else -> "Игра продолжается. Ход: ${playersList.getOrNull(currentPlayerIndex)?.name ?: "?"}"
        }

        return GameState(
            players = playersList,
            currentPlayerId = currentPlayerId,
            gameOver = gameOver,
            message = message,
        )
    }

    fun processStartGameCommand() {
        return if (gameInProgress) {
            GameEvent.Error("Игра уже идёт")
        } else {
            try {
                val gameState = startGame()
                GameEvent.GameStarted(gameState)
            } catch (e: Exception) {
            }
        }
    }

    fun processCommand(command: GameCommand): GameEvent {
        return when (command) {
            is GameCommand.Join -> {
                val player = addPlayer(command.playerName)
                GameEvent.Info("")
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
                        GameEvent.CardDealt(command.playerId, card)
                    } catch (e: Exception) {
                        GameEvent.Error(e.message ?: "Неизвестная ошибка")
                    }
                }
            }

            is GameCommand.Stand -> {
                if (gameInProgress) {
                    GameEvent.Error("Игра ещё не началась")
                } else {
                    try {
                        stand(command.playerId)
                        GameEvent.PlayerStood(command.playerId)
                    } catch (e: Exception) {
                        GameEvent.Error(e.message ?: "Неизвестная ошибка")
                    }
                }
            }
        }
    }
}
