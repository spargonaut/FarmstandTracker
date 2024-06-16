package com.farmstandtracker

import com.farmstandtracker.model.PostgresFarmstandRepository
import com.farmstandtracker.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization(PostgresFarmstandRepository())
    configureDatabases()
    configureRouting()
}
