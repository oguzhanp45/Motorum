package com.oguzhanp.motorum.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oguzhanp.motorum.data.AyarlarDeposu
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val depo: AyarlarDeposu
) : ViewModel() {

    // null = henuz okumadik. false/true = cevap geldi.
    // Uc durumu tek alanda tutuyoruz; MainActivity splash'i null iken bekletiyor.
    private val _onboardingBitti = MutableStateFlow<Boolean?>(null)
    val onboardingBitti = _onboardingBitti.asStateFlow()

    init {
        viewModelScope.launch {
            _onboardingBitti.value = depo.onboardingBittiMi()
        }
    }

    fun tamamla() {
        viewModelScope.launch { depo.onboardingiTamamla() }
    }
}
