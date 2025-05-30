package model.serialization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlayerWithoutHandAndScore(
    val id: String,
    val name: String,
    val isStanding: Boolean,
    @SerialName("cards_in_hand")
    val handSize: Int
)