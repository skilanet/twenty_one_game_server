package model.game

import kotlinx.serialization.Serializable

@Serializable
data class Card(val value: String, val suit: Suit, val rank: model.game.Rank)