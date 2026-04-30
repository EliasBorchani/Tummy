package com.tummy.utilities.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base MVI multiplatform.
 *
 * State  : immutable, rendu vers l'UI.
 * Intent : entrées utilisateur -> [onIntent].
 * Event  : actions one-shot (navigation, toast, haptic...).
 *
 * Exposé via [state] (StateFlow) et [events] (SharedFlow) — SKIE les convertit
 * automatiquement en AsyncSequence côté Swift.
 */
abstract class BaseViewModel<State : Any, Intent : Any, Event : Any>(
    initialState: State,
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _events = MutableSharedFlow<Event>(extraBufferCapacity = 16)
    val events: SharedFlow<Event> = _events.asSharedFlow()

    protected val scope: CoroutineScope get() = viewModelScope

    abstract fun onIntent(intent: Intent)

    protected fun updateState(reducer: (State) -> State) {
        _state.update(reducer)
    }

    protected fun emitEvent(event: Event): Job = scope.launch { _events.emit(event) }
}
