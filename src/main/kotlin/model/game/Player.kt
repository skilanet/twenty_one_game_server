package model.game

import kotlinx.serialization.Serializable

@Serializable
class Player(
    val id: String,
    val name: String,
    private val hand: MutableList<Card> = mutableListOf(),
    var score: Int = 0,
    var isStanding: Boolean = false,
) {
    private fun calculateHand(): Int {
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
}
