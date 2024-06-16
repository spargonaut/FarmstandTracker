package com.farmstandtracker.model

import com.farmstandtracker.db.FarmstandDAO
import com.farmstandtracker.db.FarmstandTable
import com.farmstandtracker.db.daoToModel
import com.farmstandtracker.db.suspendTransaction
import kotlinx.datetime.toJavaLocalDate
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

class PostgresFarmstandRepository : FarmstandRepository {
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

    override suspend fun addFarmstand(farmstand: Farmstand): Unit = suspendTransaction {
        FarmstandDAO.new {
            name = farmstand.name
            initDate = farmstand.initDate.toJavaLocalDate()
        }
    }

    override suspend fun removeFarmstand(name: String): Boolean = suspendTransaction {
        val rowsDeleted = FarmstandTable.deleteWhere {
            FarmstandTable.name eq name
        }
        rowsDeleted == 1
    }

    override suspend fun shutdownFarmstand(name: String, farmstandShutdown: FarmstandShutdown): Boolean {
        // TODO ("not implemented yet")
        return false;
    }
}