package websoket

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.json.Json
import model.game.GameManager
import model.serialization.forPlayer
import model.serialization.withHands
import org.koin.ktor.ext.inject
import websoket.state.GameCommand
import websoket.state.GameEvent
import websoket.state.gameModule
import java.util.concurrent.ConcurrentHashMap

fun Application.configureWebSocket() {
    val gameManager by inject<GameManager>()
    val json = Json {
        classDiscriminator = "type"
        serializersModule = gameModule
        prettyPrint = true
    }
    val connections = ConcurrentHashMap<String, WebSocketServerSession>()
    val playerGameMap = ConcurrentHashMap<String, String>()
    val playerIdToName = ConcurrentHashMap<String, String>()

    suspend fun broadcastToGame(gameId: String, event: GameEvent, excludePlayerId: String? = null) {
        val game = gameManager.getGameById(gameId) ?: return
        game.playerIds.forEach { playerId ->
            if (playerId != excludePlayerId) {
                connections[playerId]?.send(json.encodeToString(GameEvent.serializer(), event))
            }
        }
    }

    suspend fun broadcastToAllInGame(gameId: String, event: GameEvent) {
        val game = gameManager.getGameById(gameId) ?: return
        game.playerIds.forEach { playerId ->
            if (event is GameEvent.GameStarted) {
                val eventForPlayer = event.forPlayer(playerId)
                connections[playerId]?.send(eventForPlayer)
                return@forEach
            }
            if (event is GameEvent.GameOver) {
                val eventForPlayer = event.withHands()
                connections[playerId]?.send(eventForPlayer)
                return@forEach
            }
            connections[playerId]?.send(json.encodeToString(GameEvent.serializer(), event))
        }
    }

    routing {
        webSocket("/game") {
            var currentPlayerId: String? = null
            try {
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val text = frame.readText()
                            val command = json.decodeFromString<GameCommand>(text)
                            when (command) {
                                is GameCommand.Join -> {
                                    val (gameId, game) = gameManager.getOrCreateAvailableGame()
                                    val player = game.addPlayer(command.playerName)
                                    currentPlayerId = player.id

                                    connections[player.id] = this
                                    playerGameMap[player.id] = gameId
                                    playerIdToName[player.id] = player.name

                                    // Отправляем игроку его данные
                                    send(
                                        json.encodeToString(
                                            GameEvent.serializer(),
                                            GameEvent.PlayerJoined(player, gameId)
                                        )
                                    )

                                    // Уведомляем других игроков
                                    broadcastToGame(
                                        gameId,
                                        GameEvent.Info("${player.name} присоединился к игре"),
                                        player.id
                                    )

                                    if (game.playersCount == 2) {
                                        // Автоматически начинаем игру
                                        val gameState = game.startGame()
                                        broadcastToAllInGame(gameId, GameEvent.GameStarted(gameState))
                                    }
                                }

                                is GameCommand.Hit -> {
                                    currentPlayerId?.let { playerId ->
                                        playerGameMap[playerId]?.let { gameId ->
                                            val game = gameManager.getGameById(gameId)
                                            game?.let {
                                                val event = it.processCommand(GameCommand.Hit(playerId))
                                                if (event is GameEvent.CardDealt) {
                                                    connections[playerId]?.send(
                                                        json.encodeToString(
                                                            GameEvent.serializer(),
                                                            event
                                                        )
                                                    )
                                                    val opponentId = it.getOpponentId(playerId)
                                                    connections[opponentId]?.send(json.encodeToString(GameEvent.serializer(),
                                                        GameEvent.AnotherPlayerTookCard))
                                                } else {
                                                    broadcastToAllInGame(gameId, event)
                                                }
                                            }
                                        }
                                    }
                                }

                                is GameCommand.Stand -> {
                                    currentPlayerId?.let { playerId ->
                                        playerGameMap[playerId]?.let { gameId ->
                                            val game = gameManager.getGameById(gameId)
                                            game?.let {
                                                val event = it.processCommand(GameCommand.Stand(playerId))
                                                broadcastToAllInGame(gameId, event)
                                            }
                                        }
                                    }
                                }

                                else -> {}
                            }
                        }

                        else -> {}
                    }
                }
            } catch (_: ClosedReceiveChannelException) {
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                currentPlayerId?.let { playerId ->
                    connections.remove(playerId)
                    playerGameMap[playerId]?.let { gameId ->
                        val playerName = playerIdToName[playerId] ?: "Игрок"
                        broadcastToGame(gameId, GameEvent.Info("$playerName покинул игру"), playerId)
                        playerGameMap.remove(playerId)
                        playerIdToName.remove(playerId)

                        // Проверяем, нужно ли удалить игру
                        val game = gameManager.getGameById(gameId)
                        if (game?.playersCount == 1 || game?.playersCount == 0) {
                            gameManager.removeGame(gameId)
                        }
                    }
                }
            }
        }
    }
}