package com.tummy.features.settings

import com.tummy.domain.user.model.UserProfile
import com.tummy.domain.user.usecase.ObserveCurrentUserUseCase
import com.tummy.utilities.presentation.BaseViewModel
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class SettingsState(val user: UserProfile? = null)
sealed interface SettingsIntent { data object Load : SettingsIntent }
sealed interface SettingsEvent

class SettingsViewModel(
    private val observeUser: ObserveCurrentUserUseCase,
) : BaseViewModel<SettingsState, SettingsIntent, SettingsEvent>(SettingsState()) {

    init { onIntent(SettingsIntent.Load) }

    override fun onIntent(intent: SettingsIntent) {
        when (intent) { SettingsIntent.Load -> load() }
    }

    private fun load() {
        scope.launch {
            observeUser()
                .onEach { user -> updateState { it.copy(user = user) } }
                .collect { /* */ }
        }
    }
}
