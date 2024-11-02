package com.farmstandtracker.domain.measurements

interface MeasurementRepository {
    suspend fun add(theFarmstandId: Int, newFarmstandMeasurement: NewFarmstandMeasurement): Int
    suspend fun allMeasurements(farmstandId: Int): List<FarmstandMeasurement>
}