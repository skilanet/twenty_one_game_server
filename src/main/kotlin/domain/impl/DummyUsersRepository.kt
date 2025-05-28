package domain.impl

import domain.repository.UsersRepository
import model.game.User
import model.requests.UserInfo
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class DummyUsersRepository : UsersRepository {
    private data class DummyUser(
        val countOfGames: Int,
        val winRates: Float
    )

    private fun UserInfo.toDummy() = DummyUser(
        countOfGames = countOfGames,
        winRates = if (countOfGames == 0) 100f else
            countOfWins / countOfGames * 100f
    )

    private val dummyUsers = mutableMapOf<Uuid, DummyUser>(
        Uuid.parse("7cbdd1a5-e6fd-4b00-814a-7db0607e8a4d") to DummyUser(countOfGames = 0, winRates = 0f),
        Uuid.parse("1558f972-b635-4ced-bec9-0cff29a052c2") to DummyUser(countOfGames = 0, winRates = 0f),
        Uuid.parse("de8f94bd-78a0-42a9-b824-9c569e49d4b4") to DummyUser(countOfGames = 0, winRates = 0f),
        Uuid.parse("55ef6552-f7c0-4cfe-90dc-0f27b42a4541") to DummyUser(countOfGames = 0, winRates = 0f),
        Uuid.parse("e587cac7-13f0-4ed8-ad69-8447ea40b736") to DummyUser(countOfGames = 0, winRates = 0f),
    )

    private fun Map.Entry<Uuid, DummyUser>.toUser(): User = User(
        uuid = key,
        countOfGames = value.countOfGames,
        countOfWins = (value.winRates * value.countOfGames / 100).toInt(),
    )

    override suspend fun createUser(): Uuid {
        val uuid = Uuid.random()
        val user = UserInfo(0, 0)
        dummyUsers[uuid] = user.toDummy()
        return uuid
    }

    override suspend fun getConnectedUsers(uuids: Set<Uuid>): List<User> =
        dummyUsers
            .filter { it.key in uuids }
            .map { it.toUser() }

    override suspend fun updateUser(uuid: Uuid, user: UserInfo): Boolean {
        if (dummyUsers[uuid] == null) {
            return false
        }
        dummyUsers[uuid] = user.toDummy()
        return true
    }

    override suspend fun deleteUser(uuid: Uuid): Boolean {
        return dummyUsers.remove(uuid) != null
    }

    override suspend fun allUsers(): List<User> =
        dummyUsers.map { it.toUser() }
}