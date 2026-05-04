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
 * Multiplatform MVI base.
 *
 * State  : reactive, derived from upstream flows via `combine(...).stateIn(...)`.
 *          No `updateState` reducer — state is never mutable from the VM. For
 *          imperative bits (text input, UI toggles), use a private
 *          `MutableStateFlow` that flows into the combine.
 * Intent : user input -> [onIntent].
 * Event  : one-shot actions (navigation, toast, haptic...).
 *
 * Exposed via [state] (StateFlow) and [events] (SharedFlow) — SKIE auto-converts
 * them to AsyncSequence on the Swift side.
 */
abstract class BaseViewModel<State : Any, Intent : Any, Event : Any> : ViewModel() {
    abstract val state: StateFlow<State>

    private val _events = MutableSharedFlow<Event>(extraBufferCapacity = 16)
    val events: SharedFlow<Event> = _events.asSharedFlow()

    protected val scope: CoroutineScope get() = viewModelScope

    abstract fun onIntent(intent: Intent)

    protected fun emitEvent(event: Event): Job = scope.launch { _events.emit(event) }
}
