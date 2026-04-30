package com.tummy.domain.user.model

data class UserProfile(
    val id: UserId,
    val displayName: String,
    val dailyCalorieGoal: Int,
)

data class UserId(val raw: String)
