package model.response

import kotlinx.serialization.Serializable
import model.game.Suit

@Serializable
data class CardDto(val value: String, val suit: Suit, val price: Int)