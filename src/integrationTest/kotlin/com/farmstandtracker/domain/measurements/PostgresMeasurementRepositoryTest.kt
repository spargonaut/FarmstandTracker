package com.farmstandtracker.domain.measurements

import com.farmstandtracker.domain.farmstand.NewFarmstand
import com.farmstandtracker.domain.farmstand.PostgresFarmstandRepository
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils
import kotlin.test.assertEquals

class PostgresMeasurementRepositoryTest {
    companion object {
        private lateinit var postgresContainer: PostgreSQLContainer<*>
        private lateinit var databaseConnection: Database

        private var testFarmstandId: Int = -1

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
                        );
                        
                        CREATE TABLE IF NOT EXISTS measurement (
                            id SERIAL PRIMARY KEY,
                            farmstand_id INTEGER,
                            measurement_date DATE,
                            context VARCHAR(20),
                            ph FLOAT,
                            temp_value INTEGER,
                            temp_metric VARCHAR(10),
                            ec FLOAT,
                            notes VARCHAR(255),
                            CONSTRAINT fk_customer
                                FOREIGN KEY(farmstand_id)
                                    REFERENCES farmstand(id)
                        );
                """
                )
            }

            val farmstandRepository = PostgresFarmstandRepository()

            val farmstandName = RandomStringUtils.randomAlphanumeric(5)
            val newFarmstand = NewFarmstand(
                name = farmstandName,
                initDate = LocalDate(2024, 2, 14),
            )

            testFarmstandId = runBlocking { farmstandRepository.addFarmstand(newFarmstand) }
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
                    TRUNCATE TABLE measurement
                """
            )
        }
    }

    @Test
    fun `should insert and fetch a measurement using the farmstands id`() {
        val measurementRepository = PostgresMeasurementRepository()

        val newMeasurement = NewFarmstandMeasurement(
            date = LocalDate(2024, 2, 14),
            context = MeasurementContext.TAP_WATER,
            ph = 5.7,
            temp = Temperature(value = 19, metric = TemperatureMetric.CELCIUS),
            ec = 2.5,
            notes = "this is just for a test"
        )

        val allMeasurements = runBlocking {
            measurementRepository.add(testFarmstandId, newMeasurement)
            measurementRepository.allMeasurements(testFarmstandId)
        }

        assertEquals(1, allMeasurements.size)
        val savedMeasurement = allMeasurements.first()
        assertEquals(newMeasurement.date, savedMeasurement.date)
        assertEquals(newMeasurement.context, savedMeasurement.context)
        assertEquals(newMeasurement.ph, savedMeasurement.ph)
        assertEquals(newMeasurement.temp, savedMeasurement.temp)
        assertEquals(newMeasurement.ec, savedMeasurement.ec)
        assertEquals(newMeasurement.notes, savedMeasurement.notes)
    }
}