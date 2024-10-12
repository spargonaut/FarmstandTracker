package com.farmstandtracker.model

interface FarmstandRepository {
    suspend fun activeFarmstands(): List<Farmstand>
    suspend fun inactiveFarmstands(): List<Farmstand>
    suspend fun allFarmstands(): List<Farmstand>
    suspend fun farmstandByName(name: String): Farmstand?
    suspend fun addFarmstand(farmstand: Farmstand)
    suspend fun removeFarmstand(name: String): Boolean
    suspend fun shutdownFarmstand(name: String, farmstandShutdown: FarmstandShutdown): Boolean
}