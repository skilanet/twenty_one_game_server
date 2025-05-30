package model.game

import kotlinx.serialization.Serializable

@Serializable
enum class Suit {
    Hearts, Diamonds, Clubs, Spades
}

@Serializable
enum class Rank(val price: Int) {
    Six(6),
    Seven(7),
    Eight(8),
    Nine(9),
    Ten(10),
    Jack(2),
    Queen(3),
    King(4),
    Ace(11),
}

class Deck {
    private val cards = mutableListOf<Card>()

    init {
        generateDeck()
    }

    private fun generateDeck() {
        cards.clear()
        Suit.entries.forEach { suit ->
            Rank.entries.forEach { rank ->
                cards += Card(rank.name, suit, rank)
            }
        }
        cards.shuffle()
    }

    fun getRandomCard(): Card {
        if (cards.isEmpty()) {
            // Если карты закончились, генерируем новую колоду
            generateDeck()
        }
        val card = cards.random()
        cards.remove(card)
        return card
    }
}