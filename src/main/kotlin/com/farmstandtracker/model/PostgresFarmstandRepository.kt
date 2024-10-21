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

    override suspend fun addFarmstand(newFarmstand: NewFarmstand): Unit = suspendTransaction {
        FarmstandDAO.new {
            name = newFarmstand.name
            initDate = newFarmstand.initDate.toJavaLocalDate()
        }
    }

    override suspend fun removeFarmstand(name: String): Boolean = suspendTransaction {
        val rowsDeleted = FarmstandTable.deleteWhere {
            FarmstandTable.name eq name
        }
        rowsDeleted == 1
    }

    override suspend fun shutdownFarmstand(name: String, farmstandShutdown: FarmstandShutdown): Boolean {
        return transaction {
            val updatedCount = FarmstandTable.update({ FarmstandTable.name eq name }) {
                it[shutdownDate] = farmstandShutdown.shutdownDate.toJavaLocalDate()
            }
            updatedCount == 1
        }
    }
}