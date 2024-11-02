package com.farmstandtracker.domain.farmstand

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

    override suspend fun farmstandById(farmstandId: Int) = farmstands.find {
        it.id.equals(farmstandId)
    }

    override suspend fun addFarmstand(newFarmstand: NewFarmstand): Int {
        val newId = Random.nextInt(0, 100000000)
        val duplicateFarmstands = allFarmstands()
            .filter { it.name == newFarmstand.name }
            .filter { it.initDate == newFarmstand.initDate }
        if (duplicateFarmstands.isNotEmpty()) {
            throw IllegalStateException("Cannot duplicate a farmstand!\n" +
                    "a farmstand named: ${newFarmstand} with init date of ${newFarmstand.initDate} already exists")
        }

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

    override suspend fun shutdownFarmstand(farmstandId: Int, farmstandShutdown: FarmstandShutdown): Boolean {
        val farmstand = farmstands.find { fs ->
            fs.id.equals(farmstandId)
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