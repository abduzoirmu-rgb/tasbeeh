package com.tasbeeh.app.presentation.islamic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class IslamicToolsUiState(
    val nameFilter: String = ""
)

@HiltViewModel
class IslamicToolsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(IslamicToolsUiState())
    val uiState: StateFlow<IslamicToolsUiState> = _uiState

    fun updateNameFilter(q: String) {
        _uiState.update { it.copy(nameFilter = q) }
    }
}
