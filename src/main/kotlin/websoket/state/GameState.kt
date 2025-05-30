package websoket.state

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import model.game.Player
import model.serialization.serializeWithCustomRules

@Serializable
data class GameState(
    val players: List<Player>,
    val currentPlayerId: String,
    val gameOver: Boolean = false,
    val message: String = "",
) {
    fun serializeForPlayer(playerId: String): String {
        val json = Json {
            prettyPrint = true
            encodeDefaults = true
        }

        val serializedPlayers = players.serializeWithCustomRules { id ->
            id == playerId || gameOver
        }

        val gameStateJson = buildJsonObject {
            put("players", Json.parseToJsonElement(serializedPlayers))
            put("currentPlayerId", currentPlayerId)
            put("gameOver", gameOver)
            put("message", message)
        }

        return json.encodeToString(gameStateJson)
    }

    fun serializeForAll(): String {
        val json = Json {
            prettyPrint = true
            encodeDefaults = true
        }

        val serializedPlayers = players.serializeWithCustomRules { true }

        val gameStateJson = buildJsonObject {
            put("players", Json.parseToJsonElement(serializedPlayers))
            put("gameOver", gameOver)
        }

        return json.encodeToString(gameStateJson)
    }
}