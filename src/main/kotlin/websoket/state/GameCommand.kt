package websoket.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class GameCommand {
    @Serializable
    @SerialName("Join")
    data class Join(val playerName: String) : GameCommand()

    @Serializable
    @SerialName("Hit")
    data class Hit(val playerId: String) : GameCommand()

    @Serializable
    @SerialName("Stand")
    data class Stand(val playerId: String) : GameCommand()

    @Serializable
    @SerialName("StartGame")
    data class StartGame(val playerId: String) : GameCommand()
}