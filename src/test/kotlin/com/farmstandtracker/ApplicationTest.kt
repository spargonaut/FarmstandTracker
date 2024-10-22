package com.farmstandtracker

import com.farmstandtracker.model.FakeFarmstandRepository
import com.farmstandtracker.model.Farmstand
import com.farmstandtracker.model.FarmstandShutdown
import com.farmstandtracker.model.NewFarmstand
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
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.*
import kotlinx.datetime.LocalDate
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import kotlin.test.assertContains

class ApplicationTest {
    @Test
    fun `active and inactive farmstands can be retrieved`() = testApplication {
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

        val farmstandOne = createNewFarmstand(
            name = "beans"
        )
        val farmstandTwo = createNewFarmstand(
            name = "lettuce"
        )
        val farmstandThreeName = "mint"
        val farmstandThree = createNewFarmstand(
            name = farmstandThreeName,
        )

        createFarmstandWithPost(client, farmstandOne)
        createFarmstandWithPost(client, farmstandTwo)
        createFarmstandWithPost(client, farmstandThree)

        val farmstandThreeShutdown = FarmstandShutdown(
            shutdownDate = LocalDate(2024, 4, 5)
        )
        client.post("/${farmstandThreeName}/shutdown") {
            header(
                HttpHeaders.ContentType,
                ContentType.Application.Json
            )

            setBody(farmstandThreeShutdown)
        }

        val response2 = client.get("/farmstand/all")
        assertEquals(HttpStatusCode.OK, response2.status)

        val farmstandsNames = response2
            .body<List<Farmstand>>()
            .map { it.name }

        assertContains(farmstandsNames, farmstandOne.name)
        assertContains(farmstandsNames, farmstandTwo.name)
        assertContains(farmstandsNames, farmstandThree.name)
    }

    @Test
    fun `active farmstands can be retrieved`() = testApplication {
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

        val farmstandOne = createNewFarmstand(
            name = "beans"
        )
        val farmstandTwo = createNewFarmstand(
            name = "lettuce"
        )
        val farmstandThreeName = "mint"
        val farmstandThree = createNewFarmstand(
            name = farmstandThreeName,
        )

        createFarmstandWithPost(client, farmstandOne)
        createFarmstandWithPost(client, farmstandTwo)
        createFarmstandWithPost(client, farmstandThree)

        val farmstandThreeShutdown = FarmstandShutdown(
            shutdownDate = LocalDate(2024, 4, 5)
        )
        client.post("/${farmstandThreeName}/shutdown") {
            header(
                HttpHeaders.ContentType,
                ContentType.Application.Json
            )

            setBody(farmstandThreeShutdown)
        }

        val response2 = client.get("/farmstand")
        assertEquals(HttpStatusCode.OK, response2.status)

        val farmstandsNames = response2
            .body<List<Farmstand>>()

        farmstandsNames.forEach {
            assertNull(it.shutdownDate)
        }
    }

    @Test
    fun `inactive farmstands can be retrieved`() = testApplication {
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

        val farmstandOne = createNewFarmstand(
            name = "beans"
        )

        val farmstandTwoName = "mint"
        val farmstandTwo = createNewFarmstand(
            name = farmstandTwoName,
        )

        createFarmstandWithPost(client, farmstandOne)
        createFarmstandWithPost(client, farmstandTwo)

        val response2 = client.get("/farmstand/shutdown")
        assertEquals(HttpStatusCode.OK, response2.status)

        val farmstandsNames = response2
            .body<List<Farmstand>>()

        farmstandsNames.forEach {
            assertNotNull(it.shutdownDate)
        }
    }

    @Test
    fun `farmstands can be deleted by id`() = testApplication {
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

        val initialResponse = client.get("/farmstand/all")
        assertEquals(HttpStatusCode.OK, initialResponse.status)
        val originalFarmstandsNames = initialResponse
            .body<List<Farmstand>>()
            .map { it.name }

        val farmstand = createNewFarmstand()
        val response = createFarmstandWithPost(client, farmstand)
        val farmstandId = response.body<Int>()

        val responseAfterAdding = client.get("/farmstand/all")
        assertEquals(HttpStatusCode.OK, responseAfterAdding.status)
        val farmstandsIds = responseAfterAdding
            .body<List<Farmstand>>()
            .map { it.id }
        assertContains(farmstandsIds, farmstandId)

        val deleteResponse =  client.delete("/farmstand/${farmstandId}")
        assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

        val responseAfterDeleting = client.get("/farmstand/all")
        assertEquals(HttpStatusCode.OK, responseAfterDeleting.status)
        val farmstandNamesAfterDeleting = responseAfterDeleting
            .body<List<Farmstand>>()
            .map { it.name }
        assertEquals(farmstandNamesAfterDeleting, originalFarmstandsNames)
    }

    @Test
    fun `creating a farmstand should return the ID`() = testApplication {
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

        val newFarmstand = createNewFarmstand()
        val response = createFarmstandWithPost(client, newFarmstand)

        val id = response.body<Int>()

        assertEquals(HttpStatusCode.Created, response.status)
        assertThat(id, instanceOf(Int::class.java))
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

        val farmstand = createNewFarmstand()
        createFarmstandWithPost(client, farmstand)

        val urlString = "farmstand/${farmstand.name}"
        val retrievedFarmstand = client.get(urlString).body<Farmstand>()

        assertEquals(farmstand.name, retrievedFarmstand.name)
        assertEquals(farmstand.initDate, retrievedFarmstand.initDate)
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

        val urlString = "/farmstand/bad-name"
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

        val farmstand = createNewFarmstand()
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

        val farmstand = createNewFarmstand()
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

    private fun createNewFarmstand(
        name: String = "swimming",
        initDate: LocalDate = LocalDate(2024, 4, 1),
    ) = NewFarmstand(name, initDate)

    private suspend fun createFarmstandWithPost(client: HttpClient, farmstand: NewFarmstand): HttpResponse {
        val response = client.post("/farmstand") {
            header(
                HttpHeaders.ContentType,
                ContentType.Application.Json
            )

            setBody(farmstand)
        }
        assertEquals(HttpStatusCode.Created, response.status)
        return response
    }
}
