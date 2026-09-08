package com.oguzhanp.motorum.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oguzhanp.motorum.data.KayitDeposu
import com.oguzhanp.motorum.data.KayitSonucu
import com.oguzhanp.motorum.model.Kayit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KayitViewModel @Inject constructor(
    private val depo: KayitDeposu
) : ViewModel() {

    private val _uiState = MutableStateFlow(KayitUiState())
    val uiState = _uiState.asStateFlow()

    //MutableStateFlow'un .value'suna dışarıdan yazılabilir.
    // Onu private yapıp dışarıya sadece okunabilir StateFlow versiyonunu açıyoruz.
    // Böylece bir UI dosyası kazara viewModel.kayitlar.value = yazamaz

    fun yukle() {
        viewModelScope.launch {
            _uiState.update { it.copy(yukleniyor = true, hata = null) }
            yaz(depo.kayitlariGetir())
        }
    }

    fun sil(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(yukleniyor = true, hata = null) }
            val silmeHatasi = depo.sil(id)
            val sonuc = depo.kayitlariGetir()
            yaz(sonuc.copy(hata = silmeHatasi ?: sonuc.hata))
        }
    }

    private fun yaz(sonuc: KayitSonucu) {
        _uiState.value = KayitUiState(
            kayitlar = sonuc.kayitlar,
            // Toplamlar her zaman listeden turer.
            toplamTutar = sonuc.kayitlar.sumOf { it.tutar },
            // litre sadece yakit kayitlarinda var, once o tipe suzuluyor.
            toplamLitre = sonuc.kayitlar.filterIsInstance<Kayit.Yakit>().sumOf { it.litre },
            // mesafe devam eden yolculukta 0 donduruyor, toplama etkisi yok.
            toplamKm = sonuc.kayitlar.filterIsInstance<Kayit.RoadTrip>().sumOf { it.mesafe },
            yukleniyor = false,
            hata = sonuc.hata
        )
    }
}
