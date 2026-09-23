package com.oguzhanp.motorum.feature.kimlik

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oguzhanp.motorum.data.kimlik.KimlikDeposu
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
        _uiState.update { it.copy(form = yeni, hata = null, bilgi = null) }
    }

    fun girisYap() {
        val kontrol = _uiState.value.form.dogrula()
        if (!kontrol.gecerli) {
            _uiState.update { it.copy(form = kontrol) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(yukleniyor = true, hata = null, bilgi = null) }
            // Depo basarida null, hatada Turkce mesaj donduruyor.
            val hata = depo.girisYap(kontrol.eposta.trim(), kontrol.sifre)
            _uiState.update {
                it.copy(yukleniyor = false, hata = hata, basarili = hata == null)
            }
        }
    }

    // "Sifremi unuttum": yazili e-postaya Firebase'in sifre yenileme postasini
    // gonderiyor. Ayri bir ekran yok; e-posta alani bos ya da bozuksa once onu istiyoruz.
    fun sifreSifirla() {
        val form = _uiState.value.form
        if (!form.epostaGecerli) {
            _uiState.update {
                it.copy(form = form.copy(epostaHatali = true), hata = null, bilgi = null)
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(yukleniyor = true, hata = null, bilgi = null) }
            val eposta = form.eposta.trim()
            val hata = depo.sifreSifirla(eposta)
            _uiState.update {
                it.copy(
                    yukleniyor = false,
                    hata = hata,
                    bilgi = if (hata == null) "Şifre yenileme bağlantısı $eposta adresine gönderildi" else null
                )
            }
        }
    }
}
