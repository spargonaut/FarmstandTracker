package com.farmstandtracker.db

import com.farmstandtracker.model.Farmstand
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object FarmstandTable : IntIdTable("farmstand") {
    val name = varchar("name", 255)
    val initDate = date("init_date")
    val shutdownDate = date("shutdown_date").nullable()
}

class FarmstandDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<FarmstandDAO>(FarmstandTable)

    var name by FarmstandTable.name
    var initDate by FarmstandTable.initDate
    var shutdownDate by FarmstandTable.shutdownDate
}

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)


fun daoToModel(dao: FarmstandDAO) = Farmstand(
    name = dao.name,
    initDate = LocalDate(dao.initDate.year, dao.initDate.month, dao.initDate.dayOfMonth),
    shutdownDate = dao.shutdownDate?.toKotlinLocalDate()
)