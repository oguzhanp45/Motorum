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
        _uiState.update {
            val yeniListe = it.kayitlar + kayit
            it.copy(
                kayitlar = yeniListe,
                // Toplamlar her zaman listeden turer. Birikimli toplasaydik
                // silme ve duzenleme geldiginde toplam listeyle uyusmazdi.
                // oyuzden degistirildi.
                toplamTutar = yeniListe.sumOf { k -> k.tutar },
                toplamLitre = yeniListe.sumOf { k -> k.litre }
            )
        }
    }

}
