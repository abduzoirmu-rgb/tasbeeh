package com.tasbeeh.app.presentation.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tasbeeh.app.domain.usecase.PremiumAccessUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val premiumAccessUseCase: PremiumAccessUseCase
) : ViewModel() {

    val isPremium: StateFlow<Boolean> = premiumAccessUseCase.isPremium()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** Stub: реальная покупка через Play Billing будет добавлена позже. */
    fun purchase() {
        viewModelScope.launch {
            premiumAccessUseCase.unlock()
        }
    }
}
