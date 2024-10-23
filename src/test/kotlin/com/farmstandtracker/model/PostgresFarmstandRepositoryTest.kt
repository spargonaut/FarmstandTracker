package com.farmstandtracker.model

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils
import kotlin.test.assertContains


class PostgresFarmstandRepositoryTest {
    companion object {
        private lateinit var postgresContainer: PostgreSQLContainer<*>
        private lateinit var databaseConnection: Database

        @BeforeAll
        @JvmStatic
        fun setup() {
            val databaseName = "farmstand_tracker"
            val username = "farmer"
            val password = "mysecretpassword"

            postgresContainer = PostgreSQLContainer<Nothing>("postgres:13.3").apply {
                withDatabaseName(databaseName)
                withUsername(username)
                withPassword(password)
                start()
            }

            databaseConnection = Database.connect(
                url = postgresContainer.jdbcUrl,
                user = username,
                password = password,

                )
            transaction(databaseConnection) {
                exec(
                    """
                        CREATE TABLE IF NOT EXISTS farmstand (
                            id SERIAL PRIMARY KEY,
                            name VARCHAR(50) NOT NULL,
                            init_date DATE NOT NULL,
                            shutdown_date DATE
                        )
                """
                )
            }

            val bar = 1 * 3
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            TransactionManager.closeAndUnregister(databaseConnection)
            postgresContainer.stop()
        }

    }

    @AfterEach
    fun cleanup() {
        transaction(databaseConnection) {
            exec(
                """
                    TRUNCATE TABLE farmstand
                """
            )
        }
    }

    @Test
    fun `should insert and retrieve a farmstand by name`() {
        val repository = PostgresFarmstandRepository()

        val farmstandName = RandomStringUtils.randomAlphanumeric(5)
        val newFarmstand = NewFarmstand(
            name = farmstandName,
            initDate = LocalDate(2024, 2, 14),
        )

        val fetchedFarmstand = runBlocking {
            repository.addFarmstand(newFarmstand)
            repository.farmstandByName(farmstandName)
        }

        assertEquals(newFarmstand.name, fetchedFarmstand?.name)
        assertEquals(newFarmstand.initDate, fetchedFarmstand?.initDate)
    }

    @Test
    fun `should shutdown a farmstand`() {
        val repository = PostgresFarmstandRepository()

        val farmstandName = RandomStringUtils.randomAlphanumeric(5)
        val newFarmstand = NewFarmstand(
            name = farmstandName,
            initDate = LocalDate(2024, 2, 14),
        )
        val farmstandId = runBlocking { repository.addFarmstand(newFarmstand) }

        val shutdownDate = LocalDate(2024, 2, 16)
        val farmstandShutdown = FarmstandShutdown(shutdownDate)

        val fetchedFarmstand = runBlocking {
            repository.shutdownFarmstand(farmstandId, farmstandShutdown)
            repository.farmstandByName(farmstandName)
        }

        assertEquals(shutdownDate, fetchedFarmstand?.shutdownDate)
    }

    @Test
    fun `should fetch only active farmstands`() {
        val repository = PostgresFarmstandRepository()

        val activeFarmstandOneName = RandomStringUtils.randomAlphanumeric(5)
        val activeFarmstandOne = NewFarmstand(
            name = activeFarmstandOneName,
            initDate = LocalDate(2024, 2, 14),
        )
        runBlocking { repository.addFarmstand(activeFarmstandOne) }

        val activeFarmstandTwoName = RandomStringUtils.randomAlphanumeric(5)
        val activeFarmstandTwo = NewFarmstand(
            name = activeFarmstandTwoName,
            initDate = LocalDate(2024, 2, 15),
        )
        runBlocking { repository.addFarmstand(activeFarmstandTwo) }

        val activeFarmstandThreeName = RandomStringUtils.randomAlphanumeric(5)
        val activeFarmstandThree = NewFarmstand(
            name = activeFarmstandThreeName,
            initDate = LocalDate(2024, 2, 15),
        )
        runBlocking { repository.addFarmstand(activeFarmstandThree) }

        val inactiveFarmstandOneName = RandomStringUtils.randomAlphanumeric(5)
        val inactiveFarmstandOne = NewFarmstand(
            name = inactiveFarmstandOneName,
            initDate = LocalDate(2024, 2, 15),
        )
        val inactiveFarmstandOneId = runBlocking { repository.addFarmstand(inactiveFarmstandOne) }

        val shutdownDate = LocalDate(2024, 2, 16)
        val inactiveFarmstandOneShutdown = FarmstandShutdown(shutdownDate)
        runBlocking {
            repository.shutdownFarmstand(
                inactiveFarmstandOneId,
                inactiveFarmstandOneShutdown
            )
        }

        val activeFarmstands = runBlocking { repository.activeFarmstands() }

        assertEquals(3, activeFarmstands.size)
        val activeFarmstandNames = activeFarmstands.map { it.name }
        assertContains(activeFarmstandNames, activeFarmstandOneName)
        assertContains(activeFarmstandNames, activeFarmstandTwoName)
        assertContains(activeFarmstandNames, activeFarmstandThreeName)
    }

    @Test
    fun `should fetch only inactive farmstands`() {
        val repository = PostgresFarmstandRepository()

        val activeFarmstandOneName = RandomStringUtils.randomAlphanumeric(5)
        val activeFarmstandOne = NewFarmstand(
            name = activeFarmstandOneName,
            initDate = LocalDate(2024, 2, 14),
        )
        runBlocking { repository.addFarmstand(activeFarmstandOne) }

        val activeFarmstandTwoName = RandomStringUtils.randomAlphanumeric(5)
        val activeFarmstandTwo = NewFarmstand(
            name = activeFarmstandTwoName,
            initDate = LocalDate(2024, 2, 15),
        )
        runBlocking { repository.addFarmstand(activeFarmstandTwo) }

        val inactiveFarmstandOneName = RandomStringUtils.randomAlphanumeric(5)
        val inactiveFarmstandOne = NewFarmstand(
            name = inactiveFarmstandOneName,
            initDate = LocalDate(2024, 2, 15),
        )
        val inactiveFarmstandOneId = runBlocking { repository.addFarmstand(inactiveFarmstandOne) }

        val shutdownDateOne = LocalDate(2024, 2, 16)
        val inactiveFarmstandOneShutdown = FarmstandShutdown(shutdownDateOne)
        runBlocking {
            repository.shutdownFarmstand(
                inactiveFarmstandOneId,
                inactiveFarmstandOneShutdown
            )
        }

        val inactiveFarmstandTwoName = RandomStringUtils.randomAlphanumeric(5)
        val inactiveFarmstandTwo = NewFarmstand(
            name = inactiveFarmstandTwoName,
            initDate = LocalDate(2024, 2, 15),
        )
        val inactiveFarmstandTwoId = runBlocking { repository.addFarmstand(inactiveFarmstandTwo) }

        val shutdownDateTwo = LocalDate(2024, 2, 17)
        val inactiveFarmstandTwoShutdown = FarmstandShutdown(shutdownDateTwo)
        runBlocking {
            repository.shutdownFarmstand(
                inactiveFarmstandTwoId,
                inactiveFarmstandTwoShutdown
            )
        }

        val inactiveFarmstands = runBlocking { repository.inactiveFarmstands() }

        assertEquals(2, inactiveFarmstands.size)
        val inactiveFarmstandNames = inactiveFarmstands.map { it.name }
        assertContains(inactiveFarmstandNames, inactiveFarmstandOneName)
        assertContains(inactiveFarmstandNames, inactiveFarmstandTwoName)
    }

    @Test
    fun `should fetch all farmstands`() {
        val repository = PostgresFarmstandRepository()

        val activeFarmstandOneName = RandomStringUtils.randomAlphanumeric(5)
        val activeFarmstandOne = NewFarmstand(
            name = activeFarmstandOneName,
            initDate = LocalDate(2024, 2, 14),
        )
        runBlocking { repository.addFarmstand(activeFarmstandOne) }

        val activeFarmstandTwoName = RandomStringUtils.randomAlphanumeric(5)
        val activeFarmstandTwo = NewFarmstand(
            name = activeFarmstandTwoName,
            initDate = LocalDate(2024, 2, 15),
        )
        runBlocking { repository.addFarmstand(activeFarmstandTwo) }

        val inactiveFarmstandOneName = RandomStringUtils.randomAlphanumeric(5)
        val inactiveFarmstandOne = NewFarmstand(
            name = inactiveFarmstandOneName,
            initDate = LocalDate(2024, 2, 15),
        )
        val inactiveFarmstandOneId = runBlocking { repository.addFarmstand(inactiveFarmstandOne) }

        val shutdownDateOne = LocalDate(2024, 2, 16)
        val inactiveFarmstandOneShutdown = FarmstandShutdown(shutdownDateOne)
        runBlocking {
            repository.shutdownFarmstand(
                inactiveFarmstandOneId,
                inactiveFarmstandOneShutdown
            )
        }

        val inactiveFarmstandTwoName = RandomStringUtils.randomAlphanumeric(5)
        val inactiveFarmstandTwo = NewFarmstand(
            name = inactiveFarmstandTwoName,
            initDate = LocalDate(2024, 2, 15),
        )
        val inactiveFarmstandTwoId = runBlocking { repository.addFarmstand(inactiveFarmstandTwo) }

        val shutdownDateTwo = LocalDate(2024, 2, 17)
        val inactiveFarmstandTwoShutdown = FarmstandShutdown(shutdownDateTwo)
        runBlocking {
            repository.shutdownFarmstand(
                inactiveFarmstandTwoId,
                inactiveFarmstandTwoShutdown
            )
        }

        val allFarmstands = runBlocking { repository.allFarmstands() }

        assertEquals(4, allFarmstands.size)
        val allFarmstandNames = allFarmstands.map { it.name }

        assertContains(allFarmstandNames, inactiveFarmstandOneName)
        assertContains(allFarmstandNames, inactiveFarmstandTwoName)

        assertContains(allFarmstandNames, activeFarmstandOneName)
        assertContains(allFarmstandNames, activeFarmstandTwoName)
    }
}