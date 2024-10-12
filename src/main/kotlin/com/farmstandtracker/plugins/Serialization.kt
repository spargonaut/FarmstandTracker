package com.farmstandtracker.plugins

import com.farmstandtracker.model.Farmstand
import com.farmstandtracker.model.FarmstandRepository
import com.farmstandtracker.model.FarmstandShutdown
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
                    val farmstand = call.receive<Farmstand>()
                    farmstandRepository.addFarmstand(farmstand)
                    call.respond(HttpStatusCode.NoContent)
                } catch (ex: IllegalStateException) {
                    call.respond(HttpStatusCode.BadRequest)
                } catch (ex: JsonConvertException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            route("/{farmstandName}") {
                get {
                    val name = call.parameters["farmstandName"]
                    if (name == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@get
                    }
                    val farmstand = farmstandRepository.farmstandByName(name)
                    if (farmstand == null) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(farmstand)
                }
                delete {
                    val name = call.parameters["farmstandName"]
                    if (name == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@delete
                    }
                    if (farmstandRepository.removeFarmstand(name)) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }

                post {
                    try {
                        val name = call.parameters["farmstandName"]
                        val farmstandShutdown = call.receive<FarmstandShutdown>()
                        if (name.isNullOrEmpty()) {
                            call.respond(HttpStatusCode.BadRequest)
                        } else if (farmstandRepository.shutdownFarmstand(name, farmstandShutdown)) {
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
