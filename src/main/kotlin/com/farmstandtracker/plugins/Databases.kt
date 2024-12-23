package com.farmstandtracker.plugins

import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabases() {
    val dbHost = System.getenv("FS_DB_HOST") ?: "localhost"
    Database.connect(
        url = "jdbc:postgresql://${dbHost}:5432/farmstand_tracker",
        user = System.getenv("FS_DB_USER") ?: "farmer",
        password = System.getenv("FS_DB_PASSWORD") ?: "mysecretpassword",
    )
}
