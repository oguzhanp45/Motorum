package com.oguzhanp.motorum.ui.kimlik

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oguzhanp.motorum.data.KimlikDeposu
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GirisViewModel @Inject constructor(
    private val depo: KimlikDeposu
) : ViewModel() {

    private val _uiState = MutableStateFlow(GirisUiState())
    val uiState = _uiState.asStateFlow()

    // Basarili bayragi tek seferlik bir olay: ekran onu gorup yonlendirdikten sonra
    // geri kapatmasi gerekiyor. Kapatmazsak ekrana her donusunde LaunchedEffect
    // yeniden tetiklenir ve kullanici giris ekranini goremeden ana sayfaya atilir.
    fun basariliTuketildi() {
        _uiState.value = GirisUiState()
    }

    fun formDegis(yeni: KimlikFormu) {
        _uiState.update { it.copy(form = yeni, hata = null) }
    }

    fun girisYap() {
        val kontrol = _uiState.value.form.dogrula()
        if (!kontrol.gecerli) {
            _uiState.update { it.copy(form = kontrol) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(yukleniyor = true, hata = null) }
            // Depo basarida null, hatada Turkce mesaj donduruyor.
            val hata = depo.girisYap(kontrol.eposta.trim(), kontrol.sifre)
            _uiState.update {
                it.copy(yukleniyor = false, hata = hata, basarili = hata == null)
            }
        }
    }
}
