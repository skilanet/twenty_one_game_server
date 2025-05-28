package model.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    @SerialName("count_of_games") val countOfGames: Int = 0,
    @SerialName("count_of_wins") val countOfWins: Int = 0,
)
