package com.farmstandtracker.model

interface MeasurementRepository {
    suspend fun add(theFarmstandId: Int, newFarmstandMeasurement: NewFarmstandMeasurement): Int
    suspend fun allMeasurements(farmstandId: Int): List<FarmstandMeasurement>
}