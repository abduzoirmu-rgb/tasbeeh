package com.tasbeeh.app.presentation.dhikr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tasbeeh.app.domain.model.Dhikr
import com.tasbeeh.app.domain.repository.DhikrRepository
import com.tasbeeh.app.domain.usecase.GetDhikrsUseCase
import com.tasbeeh.app.domain.usecase.SaveDhikrUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    fun saveDhikr(name: String, arabicText: String?, targetCount: Int) {
        viewModelScope.launch {
            saveDhikrUseCase(
                Dhikr(
                    name = name,
                    arabicText = arabicText?.takeIf { it.isNotBlank() },
                    targetCount = targetCount,
                    isCustom = true
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
