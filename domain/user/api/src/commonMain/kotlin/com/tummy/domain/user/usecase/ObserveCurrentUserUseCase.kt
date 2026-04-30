package com.tummy.domain.user.usecase

import com.tummy.domain.user.model.UserProfile
import com.tummy.domain.user.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class ObserveCurrentUserUseCase(
    private val repo: UserRepository,
) {
    operator fun invoke(): Flow<UserProfile?> = repo.observeCurrent()
}
