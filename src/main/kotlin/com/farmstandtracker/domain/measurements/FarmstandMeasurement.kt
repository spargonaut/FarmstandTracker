package com.farmstandtracker.domain.measurements

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class NewFarmstandMeasurement(
    val date: LocalDate,
    val context: MeasurementContext,
    val ph: Double,
    val temp: Temperature,
    val ec: Double,
    val notes: String
)

@Serializable
data class FarmstandMeasurement(
    val farmstandId: Int,
    val measurementId: Int,
    val date: LocalDate,
    val context: MeasurementContext,
    val ph: Double,
    val temp: Temperature,
    val ec: Double,
    val notes: String
)

@Serializable
enum class MeasurementContext {
    TAP_WATER,
    BEFORE_AMENDMENTS,
    AFTER_AMENDMENTS
}

@Serializable
data class Temperature(
    val value: Int,
    val metric: TemperatureMetric
)

@Serializable
enum class TemperatureMetric {
    CELCIUS,
    FAHRENHEIT,
}
