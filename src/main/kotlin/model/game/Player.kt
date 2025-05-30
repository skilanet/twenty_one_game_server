package model.game

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Player(
    val id: String,
    val name: String,
    var score: Int = 0,
    var isStanding: Boolean = false,
    @Transient
    private val hand: MutableList<Card> = mutableListOf()
) {
    fun calculateHand(): Int {
        var total = 0
        var aces = 0
        hand.forEach { card ->
            if (card.rank == Rank.Ace) {
                total += card.rank.price
                aces++
            } else {
                total += card.rank.price
            }
        }
        while (total > 21 && aces > 0) {
            total -= 10
            aces--
        }
        score = total
        return total
    }

    fun addCard(card: Card): Card {
        hand.add(card)
        calculateHand()
        return card
    }

    fun getHand(): List<Card> = hand.toList()

    val handSize: Int
        get() = hand.size
}