package com.tummy.features.settings

import androidx.lifecycle.viewModelScope
import com.tummy.domain.user.model.UserProfile
import com.tummy.domain.user.usecase.ObserveCurrentUserUseCase
import com.tummy.utilities.presentation.BaseViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class SettingsState(val user: UserProfile? = null)
sealed interface SettingsIntent { data object Load : SettingsIntent }
sealed interface SettingsEvent

class SettingsViewModel(
    private val observeUser: ObserveCurrentUserUseCase,
) : BaseViewModel<SettingsState, SettingsIntent, SettingsEvent>() {

    override val state: StateFlow<SettingsState> = observeUser()
        .map { SettingsState(user = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = SettingsState(),
        )

    override fun onIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.Load -> Unit
        }
    }
}
