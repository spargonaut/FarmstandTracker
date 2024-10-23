package com.farmstandtracker.model

interface FarmstandRepository {
    suspend fun activeFarmstands(): List<Farmstand>
    suspend fun inactiveFarmstands(): List<Farmstand>
    suspend fun allFarmstands(): List<Farmstand>
    suspend fun farmstandByName(name: String): Farmstand?
    suspend fun addFarmstand(newFarmstand: NewFarmstand): Int
    suspend fun removeFarmstand(farmstandId: Int): Boolean
    suspend fun shutdownFarmstand(farmstandId: Int, farmstandShutdown: FarmstandShutdown): Boolean
}