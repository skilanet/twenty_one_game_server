import db.UsersDAO
import db.UsersTable
import db.configureDatabase
import domain.impl.DummyUsersRepository
import domain.impl.UsersRepositoryImpl
import domain.repository.UsersRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import model.game.User
import model.requests.UserInfo
import model.response.CreatedUserResponse
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import routes.configureUserRoutes
import java.util.*
import kotlin.test.assertContains
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class UserRepositoryTest {

    private fun ApplicationTestBuilder.setupWithDB(repository: UsersRepository): HttpClient {
        application {
            configureUserRoutes(repository)
            configureServer()
            configureDatabase()
            transaction {
                UsersTable.deleteAll()
                SchemaUtils.create(UsersTable)
                UsersDAO.new(
                    id = UUID.fromString("7cbdd1a5-e6fd-4b00-814a-7db0607e8a4d")
                ) {
                    countOfGames = 0
                    winRates = 0f
                }
                UsersDAO.new(
                    id = UUID.fromString("1558f972-b635-4ced-bec9-0cff29a052c2")
                ) {
                    countOfGames = 0
                    winRates = 0f
                }
                UsersDAO.new(
                    id = UUID.fromString("de8f94bd-78a0-42a9-b824-9c569e49d4b4")
                ) {
                    countOfGames = 0
                    winRates = 0f
                }
                UsersDAO.new(
                    id = UUID.fromString("55ef6552-f7c0-4cfe-90dc-0f27b42a4541")
                ) {
                    countOfGames = 0
                    winRates = 0f
                }
                UsersDAO.new(
                    id = UUID.fromString("e587cac7-13f0-4ed8-ad69-8447ea40b736")
                ) {
                    countOfGames = 0
                    winRates = 0f
                }
            }
        }
        return createClient {
            install(ContentNegotiation) { json() }
        }
    }

    @DisplayName("get all users")
    @Test
    fun `get all users`() = testApplication {
        val client = setupWithDB(DummyUsersRepository())
        val response = client.get("/users/all")
        val result = response.body<List<User>>()

        assertEquals(HttpStatusCode.OK, response.status)
        val actualUuids = result.map { it.uuid.toString() }
        val expectedUuids = listOf(
            "7cbdd1a5-e6fd-4b00-814a-7db0607e8a4d",
            "1558f972-b635-4ced-bec9-0cff29a052c2",
            "de8f94bd-78a0-42a9-b824-9c569e49d4b4",
            "55ef6552-f7c0-4cfe-90dc-0f27b42a4541",
            "e587cac7-13f0-4ed8-ad69-8447ea40b736",
        )
        assertContentEquals(expectedUuids, actualUuids)
    }

    @DisplayName("create user")
    @Test
    fun `create user`() = testApplication {
        val client = setupWithDB(DummyUsersRepository())
        val info = UserInfo(15, 7)
        val response1 = client.get("/users") {
            contentType(ContentType.Application.Json)
            setBody(info)
        }
        val result1 = response1.body<CreatedUserResponse>().uuid.toString()
        assertEquals(HttpStatusCode.Created, response1.status)

        val response2 = client.get("/users/all")
        val result2 = response2.body<List<User>>().map { it.uuid.toString() }
        assertEquals(HttpStatusCode.OK, response2.status)

        assertContains(result2, result1)

    }

    @DisplayName("update user")
    @Test
    fun `update user`() = testApplication {
        val client = setupWithDB(DummyUsersRepository())

        val info = UserInfo(15, 7)
        val response1 = client.post("/users/update/7cbdd1a5-e6fd-4b00-814a-7db0607e8a4d") {
            contentType(ContentType.Application.Json)
            setBody(info)
        }
        assertEquals(HttpStatusCode.OK, response1.status)
    }

    @DisplayName("update user empty uuid 400")
    @Test
    fun `400 empty uuid`() = testApplication {
        val client = setupWithDB(DummyUsersRepository())

        val info = UserInfo(15, 7)
        val response1 = client.post("/users/update") {
            contentType(ContentType.Application.Json)
            setBody(info)
        }
        assertEquals(HttpStatusCode.BadRequest, response1.status)
    }

    @DisplayName("update user uuid invalid format 400")
    @Test
    fun `400 uuid invalid format`() = testApplication {
        val client = setupWithDB(DummyUsersRepository())

        val info = UserInfo(15, 7)
        val response1 = client.post("/users/update/7cbdd1a5814a7db0607e8a4d") {
            contentType(ContentType.Application.Json)
            setBody(info)
        }
        assertEquals(HttpStatusCode.BadRequest, response1.status)
    }

    @DisplayName("update user not found 404")
    @Test
    fun `404 user not found`() = testApplication {
        val client = setupWithDB(DummyUsersRepository())

        val info = UserInfo(15, 7)
        val response1 = client.post("/users/update/7cdbd1a5-e6fd-4000-814a-7db0607e8a4d") {
            contentType(ContentType.Application.Json)
            setBody(info)
        }
        assertEquals(HttpStatusCode.NotFound, response1.status)
    }

    @DisplayName("delete user")
    @Test
    fun `delete user`() = testApplication {
        val client = setupWithDB(DummyUsersRepository())

        val response1 = client.get("/users/all")
        val result1 = response1.body<List<User>>()
        assertEquals(HttpStatusCode.OK, response1.status)

        val response2 = client.delete("/users/${result1.first().uuid}")
        assertEquals(HttpStatusCode.NoContent, response2.status)

        val response3 = client.get("/users/all")
        val result3 = response3.body<List<User>>()
        assertEquals(HttpStatusCode.OK, response3.status)

        assertEquals(result1.size - 1, result3.size)

        assertContentEquals(result1.subList(1, result1.lastIndex + 1), result3)
    }

    @DisplayName("delete user invalid uuid 400")
    @Test
    fun `400 delete invalid uuid`() = testApplication {
        val client = setupWithDB(DummyUsersRepository())

        val response1 = client.delete("/users/invalid")
        assertEquals(HttpStatusCode.BadRequest, response1.status)
    }

    @DisplayName("delete user empty uuid 400")
    @Test
    fun `delete empty uuid`() = testApplication {
        val client = setupWithDB(DummyUsersRepository())

        val response1 = client.delete("/users/")
        assertEquals(HttpStatusCode.BadRequest, response1.status)
    }

    @DisplayName("delete user not found 404")
    @Test
    fun `delete non-existing user`() = testApplication {
        val client = setupWithDB(DummyUsersRepository())

        val response1 = client.delete("/users/7cdb1da5-e6fd-4b00-814a-7db0607e8a4d")
        assertEquals(HttpStatusCode.NotFound, response1.status)
    }
}