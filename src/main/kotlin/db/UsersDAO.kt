package db

import model.game.User
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object UsersTable : UUIDTable("clients") {
    val countOfGames = integer("count_of_games")
    val winRates = float("win_rates")
}

class UsersDAO(id: EntityID<UUID>) : UUIDEntity(id = id) {
    companion object : UUIDEntityClass<UsersDAO>(UsersTable)

    var countOfGames by UsersTable.countOfGames
    var winRates by UsersTable.winRates

    @OptIn(ExperimentalUuidApi::class)
    fun toModel(): User = User(
        uuid = Uuid.parse(id.value.toString()),
        countOfGames = countOfGames,
        countOfWins = (winRates * countOfGames / 100f).toInt()
    )
}