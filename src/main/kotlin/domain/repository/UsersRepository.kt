package domain.repository

import model.game.User
import model.requests.UserInfo
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface UsersRepository {
    suspend fun createUser(): Uuid
    suspend fun getConnectedUsers(uuids: Set<Uuid>): List<User>
    suspend fun updateUser(uuid: Uuid, user: UserInfo): Boolean
    suspend fun deleteUser(uuid: Uuid): Boolean
    suspend fun allUsers(): List<User>
}