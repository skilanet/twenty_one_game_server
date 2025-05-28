package websoket

import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.json.Json
import model.game.GameManager
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
    routing {
        webSocket("/game") {
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

                                    connections[player.id] = this
                                    playerGameMap[player.id] = gameId

                                    send(json.encodeToString(GameEvent.PlayerJoined(player, gameId)))

                                    if (game.playersCount == 2) {
                                        game.playerIds.forEach { playerId ->
                                            log.info("Присоединённые игроки $playerId")
                                            connections[playerId]?.send(json.encodeToString(GameEvent.Info("Все игроки присоединились, можно начинать! $playerId")))
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
                val playerId = connections.entries.find { it.value == this }?.key
                playerId?.let { connections.remove(it) }
            }
        }
    }
}

fun main() {
    val json = Json {
        serializersModule = gameModule
        classDiscriminator = "type"
    }

    val input = """{"type": "Join", "playerName":"Name 1"}"""
    val cmd = json.decodeFromString<GameCommand>(input)
    println(cmd)  // → Join(playerName=Name 1)

    val out = json.encodeToString<GameCommand>(GameCommand.Hit("ID-42"))
    println(out)  // → {"Hit":{"playerId":"ID-42"}}
}
