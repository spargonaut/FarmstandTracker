package com.farmstandtracker.db

import com.farmstandtracker.model.FarmstandMeasurement
import com.farmstandtracker.model.MeasurementContext
import com.farmstandtracker.model.Temperature
import com.farmstandtracker.model.TemperatureMetric
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date
import java.math.BigDecimal
import java.math.RoundingMode

object MeasurementTable : IntIdTable("measurement") {
    val farmstandId = integer("farmstand_id")
        .uniqueIndex()
        .references(FarmstandTable.id)
    val date = date("measurement_date")
    val context = varchar("context", 20)
    val ph = float("ph")
    val tempValue = integer("temp_value")
    val tempMetric = varchar("temp_metric", 10)
    val ec = float("ec")
    val notes = varchar("notes", 255)
}

class MeasurementDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MeasurementDao>(MeasurementTable)
    var farmstandId by MeasurementTable.farmstandId
    var date by MeasurementTable.date
    var context by MeasurementTable.context
    var ph by MeasurementTable.ph
    var tempValue by MeasurementTable.tempValue
    var tempMetric by MeasurementTable.tempMetric
    var ec by MeasurementTable.ec
    var notes by MeasurementTable.notes
}

fun measurementDaoToModel(dao: MeasurementDao) = FarmstandMeasurement(
    farmstandId = dao.farmstandId,
    measurementId = dao.id.value,
    date = LocalDate(dao.date.year, dao.date.monthValue, dao.date.dayOfMonth),
    context = MeasurementContext.valueOf(dao.context),
    ph = BigDecimal(dao.ph.toDouble()).setScale(2, RoundingMode.HALF_EVEN).toDouble(),
    temp = Temperature(value = dao.tempValue, metric = TemperatureMetric.valueOf(dao.tempMetric)),
    ec = BigDecimal(dao.ec.toDouble()).setScale(2, RoundingMode.HALF_EVEN).toDouble(),
    notes = dao.notes
)