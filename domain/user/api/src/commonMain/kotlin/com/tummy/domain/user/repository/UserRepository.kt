package com.tummy.domain.user.repository

import com.tummy.domain.user.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeCurrent(): Flow<UserProfile?>
    suspend fun save(profile: UserProfile)
}
