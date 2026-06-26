package com.tasbeeh.app.presentation.dhikr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tasbeeh.app.domain.model.Dhikr
import com.tasbeeh.app.domain.repository.DhikrRepository
import com.tasbeeh.app.domain.usecase.GetDhikrsUseCase
import com.tasbeeh.app.domain.usecase.SaveDhikrUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DhikrViewModel @Inject constructor(
    private val getDhikrsUseCase: GetDhikrsUseCase,
    private val saveDhikrUseCase: SaveDhikrUseCase,
    private val dhikrRepository: DhikrRepository
) : ViewModel() {

    val dhikrs: StateFlow<List<Dhikr>> = getDhikrsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveDhikr(name: String, arabicText: String?, targetCount: Int) {
        viewModelScope.launch {
            saveDhikrUseCase(
                Dhikr(
                    id          = 0L,
                    name        = name,
                    arabicText  = arabicText?.takeIf { it.isNotBlank() },
                    targetCount = targetCount,
                    isCustom    = true
                )
            )
        }
    }

    fun deleteDhikr(id: Long) {
        viewModelScope.launch {
            dhikrRepository.deleteDhikr(id)
        }
    }
}
