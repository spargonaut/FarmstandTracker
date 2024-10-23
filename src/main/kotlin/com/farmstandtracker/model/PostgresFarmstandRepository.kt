package com.farmstandtracker.model

import com.farmstandtracker.db.FarmstandDAO
import com.farmstandtracker.db.FarmstandTable
import com.farmstandtracker.db.daoToModel
import com.farmstandtracker.db.suspendTransaction
import kotlinx.datetime.toJavaLocalDate
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class PostgresFarmstandRepository : FarmstandRepository {
    override suspend fun activeFarmstands(): List<Farmstand> = suspendTransaction {
        FarmstandDAO
            .find { (FarmstandTable.shutdownDate eq null) }
            .map(::daoToModel)
    }

    override suspend fun inactiveFarmstands(): List<Farmstand> = suspendTransaction {
        FarmstandDAO
            .find { (FarmstandTable.shutdownDate neq null) }
            .map(::daoToModel)
    }

    override suspend fun allFarmstands(): List<Farmstand> = suspendTransaction {
        FarmstandDAO.all().map(::daoToModel)
    }

    override suspend fun farmstandByName(name: String): Farmstand? = suspendTransaction {
        FarmstandDAO
            .find { (FarmstandTable.name eq name) }
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    override suspend fun addFarmstand(newFarmstand: NewFarmstand): Int = suspendTransaction {
        FarmstandDAO.new {
            name = newFarmstand.name
            initDate = newFarmstand.initDate.toJavaLocalDate()
        }.id.value
    }

    override suspend fun removeFarmstand(farmstandId: Int): Boolean = suspendTransaction {
        val rowsDeleted = FarmstandTable.deleteWhere {
            FarmstandTable.id eq farmstandId
        }
        rowsDeleted == 1
    }

    override suspend fun shutdownFarmstand(farmstandId: Int, farmstandShutdown: FarmstandShutdown): Boolean {
        return transaction {
            val updatedCount = FarmstandTable.update({ FarmstandTable.id eq farmstandId }) {
                it[shutdownDate] = farmstandShutdown.shutdownDate.toJavaLocalDate()
            }
            updatedCount == 1
        }
    }
}