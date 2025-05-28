package domain.impl

import db.UsersDAO
import db.UsersTable
import db.suspendTransaction
import db.toUUID
import domain.repository.UsersRepository
import model.game.User
import model.requests.UserInfo
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

@OptIn(ExperimentalUuidApi::class)
class UsersRepositoryImpl : UsersRepository {
    override suspend fun createUser(): Uuid = suspendTransaction {
        val newUser = UsersDAO.new {
            countOfGames = 0
            winRates = 100f
        }
        newUser.id.value.toKotlinUuid()
    }

    override suspend fun getConnectedUsers(uuids: Set<Uuid>): List<User> = suspendTransaction {
        val uuidString = uuids.map { it.toUUID() }.toSet()
        UsersDAO.find { UsersTable.id inList uuidString }.map { it.toModel() }
    }

    override suspend fun updateUser(uuid: Uuid, user: UserInfo): Boolean = suspendTransaction {
        val updatedUsers = UsersTable.update({ UsersTable.id eq uuid.toUUID() }) {
            it[countOfGames] = user.countOfGames
            it[winRates] = if (user.countOfGames == 0) 100f else user.countOfWins / user.countOfGames * 100f
        }
        updatedUsers == 1
    }

    override suspend fun deleteUser(uuid: Uuid): Boolean = suspendTransaction {
        val rowDelete = UsersTable.deleteWhere { UsersTable.id eq uuid.toUUID() }
        rowDelete == 1
    }

    override suspend fun allUsers(): List<User> = suspendTransaction {
        UsersDAO.all().map { it.toModel() }
    }
}