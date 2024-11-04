package com.farmstandtracker.domain.farmstand

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.farmstand(
    farmstandRepository: FarmstandRepository
) {
    route("/farmstand") {
        get("/all") {
            val farmstands = farmstandRepository.allFarmstands()
            call.respond(farmstands)
        }

        get("/shutdown") {
            val farmstands = farmstandRepository.inactiveFarmstands()
            call.respond(farmstands)
        }

        get {
            val farmstands = farmstandRepository.activeFarmstands()
            call.respond(farmstands)
        }

        post {
            try {
                val newFarmstand = call.receive<NewFarmstand>()
                val farmstandId = farmstandRepository.addFarmstand(newFarmstand)
                call.respond(HttpStatusCode.Created, farmstandId)
            } catch (ex: IllegalStateException) {
                call.respond(HttpStatusCode.BadRequest)
            } catch (ex: JsonConvertException) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        route("/{farmstandId}") {
            get {
                val farmstandId = call.parameters["farmstandId"]?.toIntOrNull()
                if (farmstandId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                val farmstand = farmstandRepository.farmstandById(farmstandId)
                if (farmstand == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }
                call.respond(farmstand)
            }

            delete {
                val farmstandId = call.parameters["farmstandId"]?.toIntOrNull()
                if (farmstandId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@delete
                }

                if (farmstandRepository.removeFarmstand(farmstandId)) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            post {
                try {
                    val farmstandId = call.parameters["farmstandId"]?.toIntOrNull()
                    if (farmstandId == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@post
                    }

                    val farmstandShutdown = call.receive<FarmstandShutdown>()
                    if (farmstandRepository.shutdownFarmstand(farmstandId, farmstandShutdown)) {
                        call.respond(HttpStatusCode.Accepted)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                } catch (ex: IllegalStateException) {
                    call.respond(HttpStatusCode.BadRequest)
                } catch (ex: JsonConvertException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
    }
}