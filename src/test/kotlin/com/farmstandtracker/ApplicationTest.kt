package com.farmstandtracker

import com.farmstandtracker.model.Farmstand
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
import io.ktor.server.testing.*
import kotlinx.datetime.LocalDate
import kotlin.test.*

class ApplicationTest {
    @Test
    fun `new farmstands can be added and retrieved`() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val farmstand = createFarmstand()

        createFarmstandWithPost(client, farmstand)

        val response2 = client.get("/farmstands")
        assertEquals(HttpStatusCode.OK, response2.status)

        val farmstandsNames = response2
            .body<List<Farmstand>>()
            .map { it.name }

        assertContains(farmstandsNames, farmstand.name)
    }

    @Test
    fun `farmstands can be deleted by name`() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val initialResponse = client.get("/farmstands")
        assertEquals(HttpStatusCode.OK, initialResponse.status)
        val originalFarmstandsNames = initialResponse
            .body<List<Farmstand>>()
            .map { it.name }

        val farmstand = createFarmstand()
        createFarmstandWithPost(client, farmstand)

        val responseAfterAdding = client.get("/farmstands")
        assertEquals(HttpStatusCode.OK, responseAfterAdding.status)
        val farmstandsNames = responseAfterAdding
            .body<List<Farmstand>>()
            .map { it.name }
        assertContains(farmstandsNames, farmstand.name)

        val deleteResponse =  client.delete("/farmstands/swimming")
        assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

        val responseAfterDeleting = client.get("/farmstands")
        assertEquals(HttpStatusCode.OK, responseAfterDeleting.status)
        val farmstandNamesAfterDeleting = responseAfterDeleting
            .body<List<Farmstand>>()
            .map { it.name }
        assertEquals(farmstandNamesAfterDeleting, originalFarmstandsNames)
    }

    @Test
    fun `farmstands can be retrieved by name`() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val farmstand = createFarmstand()
        createFarmstandWithPost(client, farmstand)

        val urlString = "farmstands/byName/${farmstand.name}"
        val retrievedFarmstand = client.get(urlString).body<Farmstand>()

        assertEquals(retrievedFarmstand, farmstand)
    }

    private fun createFarmstand(
        name: String = "swimming",
        initDate: LocalDate = LocalDate(2024, 4, 1)
    ) = Farmstand(name, initDate)

    private suspend fun createFarmstandWithPost(client: HttpClient, farmstand: Farmstand) {
        val response1 = client.post("/farmstands") {
            header(
                HttpHeaders.ContentType,
                ContentType.Application.Json
            )

            setBody(farmstand)
        }
        assertEquals(HttpStatusCode.NoContent, response1.status)
    }
}
