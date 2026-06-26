package com.tasbeeh.app.presentation.dua

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tasbeeh.app.domain.model.DuaItem
import com.tasbeeh.app.domain.usecase.GetDuasByCategoryUseCase
import com.tasbeeh.app.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DuaDetailUiState(
    val duas: List<DuaItem> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class DuaDetailViewModel @Inject constructor(
    private val getDuasByCategory: GetDuasByCategoryUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DuaDetailUiState())
    val uiState: StateFlow<DuaDetailUiState> = _uiState

    private var loadJob: Job? = null

    fun loadCategory(categoryId: Int) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getDuasByCategory(categoryId).collect { duas ->
                _uiState.update { it.copy(duas = duas, isLoading = false) }
            }
        }
    }

    fun toggleFavorite(id: Long, current: Boolean) {
        viewModelScope.launch { toggleFavoriteUseCase(id, !current) }
    }
}
