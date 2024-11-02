package com.farmstandtracker.model

import com.farmstandtracker.db.MeasurementDao
import com.farmstandtracker.db.MeasurementTable
import com.farmstandtracker.db.measurementDaoToModel
import com.farmstandtracker.db.suspendTransaction
import kotlinx.datetime.toJavaLocalDate

class PostgresMeasurementRepository : MeasurementRepository {
    override suspend fun add(theFarmstandId: Int, newFarmstandMeasurement: NewFarmstandMeasurement): Int  = suspendTransaction {
        MeasurementDao
            .new {
                farmstandId = theFarmstandId
                date = newFarmstandMeasurement.date.toJavaLocalDate()
                context = newFarmstandMeasurement.context.toString()
                ph = newFarmstandMeasurement.ph.toFloat()
                tempValue = newFarmstandMeasurement.temp.value
                tempMetric = newFarmstandMeasurement.temp.metric.toString()
                ec = newFarmstandMeasurement.ec.toFloat()
                notes = newFarmstandMeasurement.notes
            }.id.value
    }

    override suspend fun allMeasurements(farmstandId: Int): List<FarmstandMeasurement> = suspendTransaction {
        MeasurementDao
            .find { MeasurementTable.farmstandId eq farmstandId }
            .map(::measurementDaoToModel)
    }
}