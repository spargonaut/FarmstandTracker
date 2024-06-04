package com.farmstandtracker.plugins

import com.farmstandtracker.model.Farmstand
import com.farmstandtracker.model.FarmstandRepository
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
        route("/farmstands") {
            get {
                val farmstands = farmstandRepository.allFarmstands()
                call.respond(farmstands)
            }

            get("/byName/{farmstandName}") {
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

            delete("/{farmstand}") {
                val name = call.parameters["farmstand"]
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
        }
    }
}
