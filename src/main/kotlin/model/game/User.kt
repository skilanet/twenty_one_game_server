package model.game

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class User(
    @SerialName("uuid") val uuid: Uuid,
    @SerialName("count_of_games") val countOfGames: Int = 0,
    @SerialName("count_of_wins") val countOfWins: Int = 0,
)