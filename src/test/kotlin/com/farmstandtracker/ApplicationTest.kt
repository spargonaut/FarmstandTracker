package com.farmstandtracker

import com.farmstandtracker.model.Farmstand
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
    fun newFarmstandsCanBeAddedAndRetrieved() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val farmstand = Farmstand("swimming", LocalDate(2024, 4, 1))
        val response1 = client.post("/farmstands") {
            header(
                HttpHeaders.ContentType,
                ContentType.Application.Json
            )

            setBody(farmstand)
        }
        assertEquals(HttpStatusCode.NoContent, response1.status)

        val response2 = client.get("/farmstands")
        assertEquals(HttpStatusCode.OK, response2.status)

        val farmstandsNames = response2
            .body<List<Farmstand>>()
            .map { it.name }

        assertContains(farmstandsNames, "swimming")
    }

    @Test
    fun farmstandsCanBeDeletedByName() = testApplication {
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

        val farmstand = Farmstand("swimming", LocalDate(2024, 4, 1))
        val response1 = client.post("/farmstands") {
            header(
                HttpHeaders.ContentType,
                ContentType.Application.Json
            )

            setBody(farmstand)
        }
        assertEquals(HttpStatusCode.NoContent, response1.status)

        val responseAfterAdding = client.get("/farmstands")
        assertEquals(HttpStatusCode.OK, responseAfterAdding.status)
        val farmstandsNames = responseAfterAdding
            .body<List<Farmstand>>()
            .map { it.name }
        assertContains(farmstandsNames, "swimming")

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
    fun `farm stands can be retrieved by name`() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val farmstandName = "swimming"
        val initDate = LocalDate(2024, 4, 1)

        val farmstand = Farmstand(farmstandName, initDate)
        val response1 = client.post("/farmstands") {
            header(
                HttpHeaders.ContentType,
                ContentType.Application.Json
            )

            setBody(farmstand)
        }
        assertEquals(HttpStatusCode.NoContent, response1.status)

        val urlString = "farmstands/byName/${farmstandName}"
        val retrievedFarmstand = client.get(urlString).body<Farmstand>()

        assertEquals(retrievedFarmstand, farmstand)
    }
}
