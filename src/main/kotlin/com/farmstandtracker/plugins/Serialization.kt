package com.farmstandtracker.plugins

import com.farmstandtracker.domain.farmstand.FarmstandRepository
import com.farmstandtracker.domain.measurements.MeasurementRepository
import com.farmstandtracker.domain.farmstand.farmstand
import com.farmstandtracker.domain.measurements.measurements
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing

fun Application.configureSerialization(
    farmstandRepository: FarmstandRepository,
    measurementRepository: MeasurementRepository
) {
    install(ContentNegotiation) {
        json()
    }
    routing {
        farmstand(farmstandRepository)
        measurements(measurementRepository)
    }
}