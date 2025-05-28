package db

import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabase() {
    Database.connect(
        url = "jdbc:postgresql://localhost:5432/clients",
        user = "skilanet",
        password = "password",
    )
}