package com.farmstandtracker.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class NewFarmstand(
    val name: String,
    val initDate: LocalDate,
)

@Serializable
data class Farmstand(
    val id: Int,
    val name: String,
    val initDate: LocalDate,
    val shutdownDate: LocalDate? = null
)

@Serializable
data class FarmstandShutdown(
    val shutdownDate: LocalDate
)