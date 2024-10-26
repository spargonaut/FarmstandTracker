package com.farmstandtracker.plugins

import com.farmstandtracker.model.FarmstandRepository
import com.farmstandtracker.model.FarmstandShutdown
import com.farmstandtracker.model.NewFarmstand
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureSerialization(farmstandRepository: FarmstandRepository) {
    install(ContentNegotiation) {
        json()
    }
    routing {
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
}
