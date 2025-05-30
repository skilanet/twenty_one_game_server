package websoket.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.game.Player
import model.response.CardDto

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
    data class CardDealt(val playerId: String, val card: CardDto) : GameEvent()

    @Serializable
    @SerialName("PlayerTurn")
    data object PlayerTurn : GameEvent()

    @Serializable
    @SerialName("PlayerStood")
    data class PlayerStood(val playerId: String) : GameEvent()

    @Serializable
    @SerialName("GameOver")
    data class GameOver(val winnerId: String?, val gameState: GameState) : GameEvent()

    @Serializable
    @SerialName("Error")
    data class Error(val message: String) : GameEvent()

    @Serializable
    @SerialName("Info")
    data class Info(val message: String) : GameEvent()

    @Serializable
    @SerialName("AnotherPlayerTookCard")
    object AnotherPlayerTookCard: GameEvent()

    @Serializable
    @SerialName("OpponentCards")
    data class OpponentCards(val opponentId: String, val cards: List<CardDto>) : GameEvent()
}

