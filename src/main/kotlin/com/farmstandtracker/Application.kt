package com.farmstandtracker

import com.farmstandtracker.model.FakeFarmstandRepository
import com.farmstandtracker.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization(FakeFarmstandRepository())
    configureDatabases()
    configureRouting()
}
