package com.oguzhanp.motorum.ui.ekle

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class KayitEkleViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(KayitEkleUiState())
    val uiState = _uiState.asStateFlow()

    fun guncelle(yeni: KayitEkleUiState) {
        _uiState.value = yeni
    }
}
