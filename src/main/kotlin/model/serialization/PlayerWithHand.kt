package model.serialization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.response.CardDto

@Serializable
data class PlayerWithHand(
    val id: String,
    val name: String,
    val score: Int,
    val isStanding: Boolean,
    val hand: List<CardDto>,
    @SerialName("cards_in_hand")
    val handSize: Int
)