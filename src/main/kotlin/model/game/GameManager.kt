package model.game

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

class GameManager {
    private val idsQueue = ConcurrentLinkedDeque<String>()

    private val gamesMap = ConcurrentHashMap<String, BlackjackGame>()

    fun createGame(): String {
        val gameId = UUID.randomUUID().toString()
        val game = BlackjackGame()
        gamesMap[gameId] = game
        idsQueue.addLast(gameId)
        return gameId
    }

    fun getOrCreateAvailableGame(): Pair<String, BlackjackGame> {
        for (gameId in idsQueue.reversed()) {
            val game = gamesMap[gameId]

            if (game != null && game.playersCount < 2 && !game.gameInProgress) {
                return gameId to game
            }
        }

        val newGameId = createGame()
        return newGameId to gamesMap[newGameId]!!
    }

    fun getGameById(gameId: String): BlackjackGame? {
        return gamesMap[gameId]
    }

    fun removeGame(gameId: String) {
        gamesMap.remove(gameId)
        idsQueue.remove(gameId)
    }

    fun cleanupFinishedOrEmptyGames() {
        val iterator = idsQueue.iterator()
        while (iterator.hasNext()) {
            val gameId = iterator.next()
            val game = gamesMap[gameId]
            if (game == null || game.isGameOver || game.playersCount == 0) {
                iterator.remove()
                gamesMap.remove(gameId)
            }
        }
    }
}