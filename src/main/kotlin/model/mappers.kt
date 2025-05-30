package model

import model.game.Card
import model.game.Player
import model.response.CardDto
import model.serialization.PlayerWithHand
import model.serialization.PlayerWithoutHandAndScore

fun Player.toPlayerWithHand(): PlayerWithHand {
    val hand = getHand().map { it.toDto() }
    return PlayerWithHand(id, name, score, isStanding, hand, handSize)
}

fun Player.toPlayerWithoutHand(): PlayerWithoutHandAndScore {
    return PlayerWithoutHandAndScore(id, name, isStanding, handSize)
}

fun Card.toDto() = CardDto(value = value, suit = suit, price = rank.price)