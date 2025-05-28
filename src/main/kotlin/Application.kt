import db.configureDatabase
import di.modelModule
import domain.impl.DummyUsersRepository
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.event.Level
import routes.configureUserRoutes
import websoket.configureWebSocket
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    val repository = DummyUsersRepository()
    configureServer()
    configureDatabase()
    configureWebSocketConnection()
    configureWebSocket()
    routing {
        configureUserRoutes(repository)

    }
    install(CallLogging) {
        level = Level.INFO
    }
    install(Koin) {
        slf4jLogger()
        modules(modelModule)
    }
}

fun Application.configureServer() {
    install(ContentNegotiation) {
        json()
    }
}

fun Application.configureWebSocketConnection() {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
        pingPeriod = 15.seconds
        timeout = 30.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
}