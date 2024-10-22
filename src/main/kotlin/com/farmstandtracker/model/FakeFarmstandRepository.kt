package com.farmstandtracker.model

import kotlinx.datetime.LocalDate
import kotlin.random.Random

class FakeFarmstandRepository : FarmstandRepository {
    private val farmstands = mutableListOf(
        Farmstand(1, "Strawberries", LocalDate(2024, 2, 14)),
        Farmstand(2, "Leafy Greens", LocalDate(2022, 10, 1), LocalDate(2023, 4, 10) )
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

    override suspend fun addFarmstand(newFarmstand: NewFarmstand): Int {
        if (farmstandByName(newFarmstand.name) != null) {
            throw IllegalStateException("Cannot duplicate farmstand names!")
        }
        val newId = Random.nextInt(0, 100000000)
        farmstands.add(
            Farmstand(
                id = newId,
                name = newFarmstand.name,
                initDate = newFarmstand.initDate,
            )
        )
        return newId
    }

    override suspend fun removeFarmstand(farmstandId: Int): Boolean {
        return farmstands.removeIf { it.id == farmstandId }
    }

    override suspend fun shutdownFarmstand(name: String, farmstandShutdown: FarmstandShutdown): Boolean {
        val farmstand = farmstands.find { fs ->
            fs.name.equals(name, ignoreCase = true)
        }

        if (farmstand == null) return false
        if (farmstand.shutdownDate != null) return false

        val shutdownFarmstand = Farmstand(
            id = farmstand.id,
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