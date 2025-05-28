package routes

import domain.repository.UsersRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.requests.UserInfo
import model.requests.Uuids
import model.response.CreatedUserResponse
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun Route.configureUserRoutes(repository: UsersRepository) {
    route("/users") {
        get("/connected") {
            val uuids = call.receive<Uuids>()
            val connectedUsers = repository.getConnectedUsers(uuids.uuids.toSet())
            if (connectedUsers.isEmpty()) {
                return@get call.respond(HttpStatusCode.NotFound)
            }
            call.respond(HttpStatusCode.OK, connectedUsers)
        }
        get("/all") {
            call.respond(HttpStatusCode.OK, repository.allUsers())
        }
        get {
            val userUuid = repository.createUser()
            call.respond(HttpStatusCode.Created, CreatedUserResponse(userUuid))
        }
        post("/update/{uuid?}") {
            val uuidString = call.parameters["uuid"]
            if (uuidString == null) {
                return@post call.respond(HttpStatusCode.BadRequest)
            }
            val uuid = try {
                Uuid.parse(uuidString)
            } catch (_: IllegalArgumentException) {
                return@post call.respond(HttpStatusCode.BadRequest)
            }
            val user = try {
                call.receive<UserInfo>()
            } catch (_: ContentTransformationException) {
                return@post call.respond(HttpStatusCode.UnsupportedMediaType)
            }
            if (repository.updateUser(uuid, user))
                call.respond(HttpStatusCode.OK)
            else
                call.respond(HttpStatusCode.NotFound)

        }
        delete("/{uuid?}") {
            val uuidString = call.parameters["uuid"]
            if (uuidString == null) {
                return@delete call.respond(HttpStatusCode.BadRequest, "UUID parameter is required")
            }
            try {
                val uuid = Uuid.parse(uuidString)
                if (repository.deleteUser(uuid)) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "UUID structure is bad")
            }
        }
    }
}