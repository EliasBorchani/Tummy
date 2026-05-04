package com.tummy.utilities.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Base MVI multiplatform.
 *
 * State  : reactive, dérivé d'upstream flows via `combine(...).stateIn(...)`.
 *          Pas de `updateState` reducer — l'état n'est jamais mutable depuis la VM.
 *          Pour les bouts impératifs (input texte, toggle UI), utiliser un
 *          `MutableStateFlow` privé qui flow dans le combine.
 * Intent : entrées utilisateur -> [onIntent].
 * Event  : actions one-shot (navigation, toast, haptic...).
 *
 * Exposé via [state] (StateFlow) et [events] (SharedFlow) — SKIE les convertit
 * automatiquement en AsyncSequence côté Swift.
 */
abstract class BaseViewModel<State : Any, Intent : Any, Event : Any> : ViewModel() {

    abstract val state: StateFlow<State>

    private val _events = MutableSharedFlow<Event>(extraBufferCapacity = 16)
    val events: SharedFlow<Event> = _events.asSharedFlow()

    protected val scope: CoroutineScope get() = viewModelScope

    abstract fun onIntent(intent: Intent)

    protected fun emitEvent(event: Event): Job = scope.launch { _events.emit(event) }
}
