package com.tummy.data.user.di

import com.tummy.domain.user.model.UserId
import com.tummy.domain.user.model.UserProfile
import com.tummy.domain.user.repository.UserRepository
import com.tummy.domain.user.usecase.ObserveCurrentUserUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.dsl.module

/**
 * Module minimal. En prod, brancher Room + DataStore ici.
 * Pour l'instant : une impl en mémoire pour débloquer features.
 */
val userModule = module {
    single<UserRepository> { InMemoryUserRepository() }
    factory { ObserveCurrentUserUseCase(get()) }
}

private class InMemoryUserRepository : UserRepository {
    private val state = MutableStateFlow<UserProfile?>(
        UserProfile(UserId("me"), "You", dailyCalorieGoal = 2200),
    )
    override fun observeCurrent(): Flow<UserProfile?> = state.asStateFlow()
    override suspend fun save(profile: UserProfile) { state.value = profile }
}
