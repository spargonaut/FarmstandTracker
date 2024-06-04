package com.farmstandtracker.model

interface FarmstandRepository {
    fun allFarmstands(): List<Farmstand>
    fun farmstandByName(name: String): Farmstand?
    fun addFarmstand(farmstand: Farmstand)
    fun removeFarmstand(name: String): Boolean
}