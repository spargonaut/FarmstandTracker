package com.farmstandtracker.plugins

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*

fun Application.configureDatabases() {
    Database.connect(
        "jdbc:postgresql://localhost:5432/farmstand_tracker",
        user = "postgres",
        password = "mysecretpassword",
    )
}
