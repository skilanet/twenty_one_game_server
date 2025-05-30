package websoket.state

data class PlayerScoreOutOfBounds(val playerId: String, val score: Int): Exception()