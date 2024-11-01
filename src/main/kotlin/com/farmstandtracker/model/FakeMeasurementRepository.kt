package com.farmstandtracker.model

import kotlinx.datetime.LocalDate
import kotlin.random.Random

class FakeMeasurementRepository: MeasurementRepository {
    private val farmstandMeasurements = initializeMeasurements()

    private fun initializeMeasurements() = mutableListOf(
            FarmstandMeasurement(
                farmstandId = 1,
                measurementId = 1,
                date = LocalDate(2024, 2, 14),
                context = MeasurementContext.TAP_WATER,
                ph = 7.3,
                temp = Temperature(value = 14, metric = TemperatureMetric.CELCIUS),
                ec = 0.0,
                notes = "just getting started"
            ),
            FarmstandMeasurement(
                farmstandId = 1,
                measurementId = 2,
                date = LocalDate(2024, 2, 14),
                context = MeasurementContext.BEFORE_AMENDMENTS,
                ph = 6.4,
                temp = Temperature(value = 20, metric = TemperatureMetric.CELCIUS),
                ec = 2.2,
                notes = "just getting started"
            ),
            FarmstandMeasurement(
                farmstandId = 1,
                measurementId = 3,
                date = LocalDate(2024, 2, 14),
                context = MeasurementContext.AFTER_AMENDMENTS,
                ph = 6.1,
                temp = Temperature(value = 20, metric = TemperatureMetric.CELCIUS),
                ec = 2.6,
                notes = "just getting started"
            )
        )

    override fun add(farmstandId: Int, newFarmstandMeasurement: NewFarmstandMeasurement): Int {

        val newId = Random.nextInt(0, 100000000)
        val newMeasurement = FarmstandMeasurement(
            farmstandId = farmstandId,
            measurementId = newId,
            date = newFarmstandMeasurement.date,
            context = newFarmstandMeasurement.context,
            ph = newFarmstandMeasurement.ph,
            temp = newFarmstandMeasurement.temp,
            ec = newFarmstandMeasurement.ec,
            notes = newFarmstandMeasurement.notes
        )

        farmstandMeasurements.add(newMeasurement)

        return newId
    }

    override fun allMeasurements(farmstandId: Int): List<FarmstandMeasurement> {
        return farmstandMeasurements.filter { it.farmstandId == farmstandId }
    }
}