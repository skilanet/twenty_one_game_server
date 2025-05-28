package websoket.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.game.Card
import model.game.Player

@Serializable
sealed class GameEvent {
    @Serializable
    @SerialName("PlayerJoined")
    data class PlayerJoined(val player: Player, val gameId: String) : GameEvent()

    @Serializable
    @SerialName("GameStarted")
    data class GameStarted(val gameState: GameState) : GameEvent()

    @Serializable
    @SerialName("CardDealt")
    data class CardDealt(val playerId: String, val card: Card) : GameEvent()

    @Serializable
    @SerialName("PlayerTurn")
    data class PlayerTurn(val playerId: String) : GameEvent()

    @Serializable
    @SerialName("PlayerStood")
    data class PlayerStood(val playerId: String) : GameEvent()

    @Serializable
    @SerialName("GameOver")
    data class GameOver(val winner: Player?, val gameState: GameState) : GameEvent()

    @Serializable
    @SerialName("Error")
    data class Error(val message: String) : GameEvent()

    @Serializable
    @SerialName("Info")
    data class Info(val message: String) : GameEvent()
}