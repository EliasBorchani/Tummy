package com.tummy.data.meal.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MealDto(
    val id: String,
    val name: String,
    val calories: Int,
    @SerialName("logged_at") val loggedAtEpochMillis: Long,
)
