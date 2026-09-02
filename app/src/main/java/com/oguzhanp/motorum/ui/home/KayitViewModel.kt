package com.oguzhanp.motorum.ui.home

import androidx.lifecycle.ViewModel
import com.oguzhanp.motorum.model.Kayit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// liste + ekleme + toplamlar

class KayitViewModel : ViewModel() {


    private val _uiState = MutableStateFlow(KayitUiState())
    val uiState = _uiState.asStateFlow()


    //MutableStateFlow'un .value'suna dışarıdan yazılabilir.
    // Onu private yapıp dışarıya sadece okunabilir StateFlow versiyonunu açıyoruz.
    // Böylece bir UI dosyası kazara viewModel.kayitlar.value = yazamaz


    fun ekle(kayit: Kayit) {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                kayitlar = state.kayitlar + kayit,
                toplamTutar = state.toplamTutar + kayit.tutar,
                toplamLitre = state.toplamLitre + kayit.litre
            )
        }

        //yani en yeni kayıt listenin en üstünde görünür.
        //Sona istersen _kayitlar.value + kayit olmalı
    }

}
