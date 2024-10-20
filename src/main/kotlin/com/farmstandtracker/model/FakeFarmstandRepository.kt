package com.farmstandtracker.model

import kotlinx.datetime.LocalDate

class FakeFarmstandRepository : FarmstandRepository {
    private val farmstands = mutableListOf(
        Farmstand("Strawberries", LocalDate(2024, 2, 14)),
        Farmstand("Leafy Greens", LocalDate(2022, 10, 1), LocalDate(2023, 4, 10) )
    )

    override suspend fun activeFarmstands(): List<Farmstand> = farmstands.filter {
        it.shutdownDate == null
    }

    override suspend fun inactiveFarmstands(): List<Farmstand> = farmstands.filter {
        it.shutdownDate != null
    }

    override suspend fun allFarmstands(): List<Farmstand> = farmstands

    override suspend fun farmstandByName(name: String) = farmstands.find {
        it.name.equals(name, ignoreCase = true)
    }

    override suspend fun addFarmstand(farmstand: Farmstand) {
        if (farmstandByName(farmstand.name) != null) {
            throw IllegalStateException("Cannot duplicate farmstand names!")
        }
        farmstands.add(farmstand)
    }

    override suspend fun removeFarmstand(name: String): Boolean {
        return farmstands.removeIf { it.name == name }
    }

    override suspend fun shutdownFarmstand(name: String, farmstandShutdown: FarmstandShutdown): Boolean {
        val farmstand = farmstands.find { fs ->
            fs.name.equals(name, ignoreCase = true)
        }

        if (farmstand == null) return false
        if (farmstand.shutdownDate != null) return false

        val shutdownFarmstand = Farmstand(
            name = farmstand.name,
            initDate = farmstand.initDate,
            shutdownDate = farmstandShutdown.shutdownDate,
        )

        farmstands.replaceAll {
            if (it.name == shutdownFarmstand.name) {
                shutdownFarmstand
            } else {
                it
            }
        }
        return true
    }
}