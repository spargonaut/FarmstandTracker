package com.farmstandtracker.model

interface MeasurementRepository {
    fun add(farmstandId: Int, newFarmstandMeasurement: NewFarmstandMeasurement): Int
}