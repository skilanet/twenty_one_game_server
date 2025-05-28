package websoket.state

import kotlinx.serialization.Serializable
import model.game.Player

@Serializable
data class GameState(
    val players: List<Player>,
    val currentPlayerId: String,
    val gameOver:  Boolean = false,
    val message: String = "",
)