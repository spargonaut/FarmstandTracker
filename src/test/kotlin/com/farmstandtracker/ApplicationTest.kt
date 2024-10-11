package com.farmstandtracker

import com.farmstandtracker.model.FakeFarmstandRepository
import com.farmstandtracker.model.Farmstand
import com.farmstandtracker.model.FarmstandShutdown
import com.farmstandtracker.plugins.configureRouting
import com.farmstandtracker.plugins.configureSerialization
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.*
import kotlinx.datetime.LocalDate
import kotlin.test.*

class ApplicationTest {
    @Test
    fun `new farmstands can be added and retrieved`() = testApplication {
        environment {
            config = MapApplicationConfig()
        }

        application {
            val repository = FakeFarmstandRepository()
            configureSerialization(repository)
            configureRouting()
        }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val farmstand = createFarmstand()

        createFarmstandWithPost(client, farmstand)

        val response2 = client.get("/farmstand")
        assertEquals(HttpStatusCode.OK, response2.status)

        val farmstandsNames = response2
            .body<List<Farmstand>>()
            .map { it.name }

        assertContains(farmstandsNames, farmstand.name)
    }

    @Test
    fun `farmstands can be deleted by name`() = testApplication {
        environment {
            config = MapApplicationConfig()
        }

        application {
            val repository = FakeFarmstandRepository()
            configureSerialization(repository)
            configureRouting()
        }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val initialResponse = client.get("/farmstand")
        assertEquals(HttpStatusCode.OK, initialResponse.status)
        val originalFarmstandsNames = initialResponse
            .body<List<Farmstand>>()
            .map { it.name }

        val farmstand = createFarmstand()
        createFarmstandWithPost(client, farmstand)

        val responseAfterAdding = client.get("/farmstand")
        assertEquals(HttpStatusCode.OK, responseAfterAdding.status)
        val farmstandsNames = responseAfterAdding
            .body<List<Farmstand>>()
            .map { it.name }
        assertContains(farmstandsNames, farmstand.name)

        val deleteResponse =  client.delete("/farmstand/swimming")
        assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

        val responseAfterDeleting = client.get("/farmstand")
        assertEquals(HttpStatusCode.OK, responseAfterDeleting.status)
        val farmstandNamesAfterDeleting = responseAfterDeleting
            .body<List<Farmstand>>()
            .map { it.name }
        assertEquals(farmstandNamesAfterDeleting, originalFarmstandsNames)
    }

    @Test
    fun `farmstands can be retrieved by name`() = testApplication {
        environment {
            config = MapApplicationConfig()
        }

        application {
            val repository = FakeFarmstandRepository()
            configureSerialization(repository)
            configureRouting()
        }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val farmstand = createFarmstand()
        createFarmstandWithPost(client, farmstand)

        val urlString = "farmstand/byName/${farmstand.name}"
        val retrievedFarmstand = client.get(urlString).body<Farmstand>()

        assertEquals(retrievedFarmstand, farmstand)
    }

    @Test
    fun `retrieving a farmstand by name with a bad name produces a NotFound response`() = testApplication {
        environment {
            config = MapApplicationConfig()
        }

        application {
            val repository = FakeFarmstandRepository()
            configureSerialization(repository)
            configureRouting()
        }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val urlString = "/farmstand/byName/bad-name"
        val responseStatus = client.get(urlString).status
        assertEquals(HttpStatusCode.NotFound, responseStatus)

    }

    @Test
    fun `farmstand can be shutdown by name`() = testApplication {
        environment {
            config = MapApplicationConfig()
        }

        application {
            val repository = FakeFarmstandRepository()
            configureSerialization(repository)
            configureRouting()
        }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val farmstand = createFarmstand()
        createFarmstandWithPost(client, farmstand)

        val farmstandShutdown = FarmstandShutdown(LocalDate(2024, 5, 6))
        val shutdownResponse = client.post("/farmstand/${farmstand.name}") {
            header(
                HttpHeaders.ContentType,
                ContentType.Application.Json
            )
            setBody(farmstandShutdown)
        }

        assertEquals(HttpStatusCode.Accepted, shutdownResponse.status)
    }

    @Test
    fun `repeated attempts to shutdown an already shutdown farmstand should be ignored`() = testApplication {
        environment {
            config = MapApplicationConfig()
        }

        application {
            val repository = FakeFarmstandRepository()
            configureSerialization(repository)
            configureRouting()
        }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val farmstand = createFarmstand()
        createFarmstandWithPost(client, farmstand)

        val originalFarmstandShutdown = FarmstandShutdown(LocalDate(2024, 5, 6))
        client.post("/farmstand/${farmstand.name}") {
            header(
                HttpHeaders.ContentType,
                ContentType.Application.Json
            )
            setBody(originalFarmstandShutdown)
        }

        val repeatedFarmstandShutdown = FarmstandShutdown(LocalDate(2024, 5, 8))
        val secondShutdownResponse = client.post("/farmstand/${farmstand.name}") {
            header(
                HttpHeaders.ContentType,
                ContentType.Application.Json
            )
            setBody(repeatedFarmstandShutdown)
        }

        assertEquals(HttpStatusCode.NotFound, secondShutdownResponse.status)
    }

    private fun createFarmstand(
        name: String = "swimming",
        initDate: LocalDate = LocalDate(2024, 4, 1)
    ) = Farmstand(name, initDate)

    private suspend fun createFarmstandWithPost(client: HttpClient, farmstand: Farmstand) {
        val response1 = client.post("/farmstand") {
            header(
                HttpHeaders.ContentType,
                ContentType.Application.Json
            )

            setBody(farmstand)
        }
        assertEquals(HttpStatusCode.NoContent, response1.status)
    }
}
