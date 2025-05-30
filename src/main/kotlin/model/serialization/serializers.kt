package model.serialization

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import model.game.Player
import model.toPlayerWithHand
import model.toPlayerWithoutHand
import websoket.state.GameEvent

fun List<Player>.serializeWithCustomRules(
    shouldIncludeHand: (id: String) -> Boolean
): String {
    val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    val elements = this.map { player ->
        if (shouldIncludeHand(player.id)) {
            json.encodeToJsonElement(PlayerWithHand.serializer(), player.toPlayerWithHand())
        } else {
            json.encodeToJsonElement(PlayerWithoutHandAndScore.serializer(), player.toPlayerWithoutHand())
        }
    }

    return json.encodeToString(JsonArray(elements))
}

fun GameEvent.GameStarted.forPlayer(playerId: String): String {
    return this.gameState.serializeForPlayer(playerId).let { serializedGameState ->
        val json = Json { prettyPrint = true }
        val gameStateJson = Json.parseToJsonElement(serializedGameState)

        val eventJson = buildJsonObject {
            put("type", "GameStarted")
            put("gameState", gameStateJson)
        }

        json.encodeToString(eventJson)
    }
}

fun GameEvent.GameOver.withHands(): String {
    return this.gameState.serializeForAll().let { serializedGameState ->
        val json = Json { prettyPrint = true }
        val gameStateJson = Json.parseToJsonElement(serializedGameState)

        val gameOverEventJson = buildJsonObject {
            put("type", "GameOver")
            put("winner", winnerId)
            put("gameState", gameStateJson)
        }

        json.encodeToString(gameOverEventJson)
    }
}