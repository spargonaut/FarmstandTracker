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
        val farmstand = Farmstand(
            name = farmstandName,
            initDate = LocalDate(2024, 2, 14),
        )

        val fetchedFarmstand = runBlocking {
            repository.addFarmstand(farmstand)
            repository.farmstandByName(farmstandName)
        }

        assertEquals(farmstand, fetchedFarmstand)
    }

    @Test
    fun `should shutdown a farmstand`() {
        val repository = PostgresFarmstandRepository()

        val farmstandName = RandomStringUtils.randomAlphanumeric(5)
        val farmstand = Farmstand(
            name = farmstandName,
            initDate = LocalDate(2024, 2, 14),
        )

        val shutdownDate = LocalDate(2024, 2, 16)
        val farmstandShutdown = FarmstandShutdown(shutdownDate)

        val fetchedFarmstand = runBlocking {
            repository.addFarmstand(farmstand)
            repository.shutdownFarmstand(farmstandName, farmstandShutdown)
            repository.farmstandByName(farmstandName)
        }

        assertEquals(shutdownDate, fetchedFarmstand?.shutdownDate)
    }

    @Test
    fun `should fetch only active farmstands`() {
        val repository = PostgresFarmstandRepository()

        val activeFarmstandOneName = RandomStringUtils.randomAlphanumeric(5)
        val activeFarmstandOne = Farmstand(
            name = activeFarmstandOneName,
            initDate = LocalDate(2024, 2, 14),
        )

        val activeFarmstandTwoName = RandomStringUtils.randomAlphanumeric(5)
        val activeFarmstandTwo = Farmstand(
            name = activeFarmstandTwoName,
            initDate = LocalDate(2024, 2, 15),
        )

        val activeFarmstandThreeName = RandomStringUtils.randomAlphanumeric(5)
        val activeFarmstandThree = Farmstand(
            name = activeFarmstandThreeName,
            initDate = LocalDate(2024, 2, 15),
        )

        val inactiveFarmstandOneName = RandomStringUtils.randomAlphanumeric(5)
        val inactiveFarmstandOne = Farmstand(
            name = inactiveFarmstandOneName,
            initDate = LocalDate(2024, 2, 15),
        )

        val shutdownDate = LocalDate(2024, 2, 16)
        val inactiveFarmstandOneShutdown = FarmstandShutdown(shutdownDate)

        val activeFarmstands = runBlocking {
            repository.addFarmstand(activeFarmstandOne)
            repository.addFarmstand(activeFarmstandTwo)
            repository.addFarmstand(activeFarmstandThree)
            repository.addFarmstand(inactiveFarmstandOne)

            repository.shutdownFarmstand(
                inactiveFarmstandOneName,
                inactiveFarmstandOneShutdown
            )

            repository.activeFarmstands()
        }

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
        val activeFarmstandOne = Farmstand(
            name = activeFarmstandOneName,
            initDate = LocalDate(2024, 2, 14),
        )

        val activeFarmstandTwoName = RandomStringUtils.randomAlphanumeric(5)
        val activeFarmstandTwo = Farmstand(
            name = activeFarmstandTwoName,
            initDate = LocalDate(2024, 2, 15),
        )

        val inactiveFarmstandOneName = RandomStringUtils.randomAlphanumeric(5)
        val inactiveFarmstandOne = Farmstand(
            name = inactiveFarmstandOneName,
            initDate = LocalDate(2024, 2, 15),
        )
        val shutdownDateOne = LocalDate(2024, 2, 16)
        val inactiveFarmstandOneShutdown = FarmstandShutdown(shutdownDateOne)

        val inactiveFarmstandTwoName = RandomStringUtils.randomAlphanumeric(5)
        val inactiveFarmstandTwo = Farmstand(
            name = inactiveFarmstandTwoName,
            initDate = LocalDate(2024, 2, 15),
        )
        val shutdownDateTwo = LocalDate(2024, 2, 17)
        val inactiveFarmstandTwoShutdown = FarmstandShutdown(shutdownDateTwo)

        val inactiveFarmstands = runBlocking {
            repository.addFarmstand(activeFarmstandOne)
            repository.addFarmstand(activeFarmstandTwo)

            repository.addFarmstand(inactiveFarmstandOne)
            repository.shutdownFarmstand(
                inactiveFarmstandOneName,
                inactiveFarmstandOneShutdown
            )

            repository.addFarmstand(inactiveFarmstandTwo)
            repository.shutdownFarmstand(
                inactiveFarmstandTwoName,
                inactiveFarmstandTwoShutdown
            )

            repository.inactiveFarmstands()
        }

        assertEquals(2, inactiveFarmstands.size)
        val inactiveFarmstandNames = inactiveFarmstands.map { it.name }
        assertContains(inactiveFarmstandNames, inactiveFarmstandOneName)
        assertContains(inactiveFarmstandNames, inactiveFarmstandTwoName)
    }

    @Test
    fun `should fetch all farmstands`() {
        val repository = PostgresFarmstandRepository()

        val activeFarmstandOneName = RandomStringUtils.randomAlphanumeric(5)
        val activeFarmstandOne = Farmstand(
            name = activeFarmstandOneName,
            initDate = LocalDate(2024, 2, 14),
        )

        val activeFarmstandTwoName = RandomStringUtils.randomAlphanumeric(5)
        val activeFarmstandTwo = Farmstand(
            name = activeFarmstandTwoName,
            initDate = LocalDate(2024, 2, 15),
        )

        val inactiveFarmstandOneName = RandomStringUtils.randomAlphanumeric(5)
        val inactiveFarmstandOne = Farmstand(
            name = inactiveFarmstandOneName,
            initDate = LocalDate(2024, 2, 15),
        )
        val shutdownDateOne = LocalDate(2024, 2, 16)
        val inactiveFarmstandOneShutdown = FarmstandShutdown(shutdownDateOne)

        val inactiveFarmstandTwoName = RandomStringUtils.randomAlphanumeric(5)
        val inactiveFarmstandTwo = Farmstand(
            name = inactiveFarmstandTwoName,
            initDate = LocalDate(2024, 2, 15),
        )
        val shutdownDateTwo = LocalDate(2024, 2, 17)
        val inactiveFarmstandTwoShutdown = FarmstandShutdown(shutdownDateTwo)

        val allFarmstands = runBlocking {
            repository.addFarmstand(activeFarmstandOne)
            repository.addFarmstand(activeFarmstandTwo)

            repository.addFarmstand(inactiveFarmstandOne)
            repository.shutdownFarmstand(
                inactiveFarmstandOneName,
                inactiveFarmstandOneShutdown
            )

            repository.addFarmstand(inactiveFarmstandTwo)
            repository.shutdownFarmstand(
                inactiveFarmstandTwoName,
                inactiveFarmstandTwoShutdown
            )

            repository.allFarmstands()
        }

        assertEquals(4, allFarmstands.size)
        val allFarmstandNames = allFarmstands.map { it.name }

        assertContains(allFarmstandNames, inactiveFarmstandOneName)
        assertContains(allFarmstandNames, inactiveFarmstandTwoName)

        assertContains(allFarmstandNames, activeFarmstandOneName)
        assertContains(allFarmstandNames, activeFarmstandTwoName)
    }
}