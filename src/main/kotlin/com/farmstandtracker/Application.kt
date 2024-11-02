package com.farmstandtracker

import com.farmstandtracker.domain.farmstand.PostgresFarmstandRepository
import com.farmstandtracker.model.PostgresMeasurementRepository
import com.farmstandtracker.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization(
        PostgresFarmstandRepository(),
        PostgresMeasurementRepository()
    )
    configureDatabases()
    configureRouting()
}
