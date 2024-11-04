package com.farmstandtracker.domain.measurements

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.measurements(
    measurementRepository: MeasurementRepository
){
    route("/{farmstandId}/measurement") {
        post {
            try {
                val farmstandId = call.parameters["farmstandId"]?.toIntOrNull()
                if (farmstandId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }

                val newFarmstandMeasurement = call.receive<NewFarmstandMeasurement>()
                val measurementId = measurementRepository.add(farmstandId, newFarmstandMeasurement)
                call.respond(HttpStatusCode.Created, measurementId)

            } catch (ex: IllegalStateException) {
                call.respond(HttpStatusCode.BadRequest)
            } catch (ex: JsonConvertException) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        get {
            val farmstandId = call.parameters["farmstandId"]?.toIntOrNull()
            if (farmstandId == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val measurements = measurementRepository.allMeasurements(farmstandId)
            call.respond(HttpStatusCode.OK, measurements)
        }

    }
}