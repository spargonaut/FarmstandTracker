package com.farmstandtracker.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Farmstand(
    val name: String,
    val initDate: LocalDate,
    val shutdownDate: LocalDate? = null
)
