package com.farmstandtracker.model

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer


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

    @Test
    fun `test inserting and retrieving a farmstand`() {
        val repository = PostgresFarmstandRepository()

        val farmstandName = "foobar"
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
}