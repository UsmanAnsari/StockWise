package com.uansari.stockwise.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base ViewModel implementing MVI pattern.
 *
 * @param State - The UI state type (data class implementing UiState)
 * @param Event - The event type (sealed class implementing UiEvent)
 * @param Effect - The side effect type (sealed class implementing UiSideEffect)
 */
abstract class BaseViewModel<State : UiState, Event : UiEvent, Effect : UiEffect>(
    initialState: State
) : ViewModel() {

    // ==================== STATE ====================

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    protected val currentState: State
        get() = _uiState.value

    protected fun updateState(reducer: State.() -> State) {
        _uiState.update { it.reducer() }
    }

    // ==================== EVENTS ====================

    /**
     * Single entry point for all UI events.
     * Must be implemented by subclasses.
     */

    abstract fun onEvent(event: Event)

    // ==================== SIDE EFFECTS ====================

    private val _sideEffect = Channel<Effect>(Channel.BUFFERED)
    val sideEffect: Flow<Effect> = _sideEffect.receiveAsFlow()

    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch {
            _sideEffect.send(effect)
        }
    }


}
