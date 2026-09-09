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

    // Asagi cekince cagriliyor. yukle() ile ayni isi yapiyor, tek farki hangi
    // bayragi kaldirdigi: liste ekranda kalsin diye.
    fun yenile() {
        viewModelScope.launch {
            _uiState.update { it.copy(yenileniyor = true, hata = null) }
            yaz(depo.kayitlariGetir())
        }
    }

    // Kaydirinca kayit gercekten siliniyor, ertelenmiyor. Geri Al ayni kaydi
    // ayni kimlikle tekrar yaziyor. yukleniyor bilerek kaldirilmadi: satir
    // zaten gitti, ustune tum listeyi daireyle degistirmek gereksiz.
    fun sil(id: String) {
        viewModelScope.launch {
            val silinen = _uiState.value.kayitlar.firstOrNull { it.id == id }

            // Satiri once ekrandan kaldiriyoruz. Bu bir basari iddiasi degil,
            // kaydirma hareketinin karsiligi. "Silindi" sozunu snackbar veriyor
            // ve o asagida hala sunucu onayini bekliyor.
            yaz(KayitSonucu(kayitlar = _uiState.value.kayitlar.filterNot { it.id == id }))

            val silmeHatasi = depo.sil(id)
            val sonuc = depo.kayitlariGetir()
            yaz(sonuc.copy(hata = silmeHatasi ?: sonuc.hata))

            if (silmeHatasi == null && silinen != null) {
                _uiState.update { it.copy(geriAlinabilir = silinen) }
            }
        }
    }

    fun geriAl() {
        val kayit = _uiState.value.geriAlinabilir ?: return
        viewModelScope.launch {
            // Once teklifi kaldiriyoruz: ayni snackbar'a iki kez basilamasin.
            _uiState.update { it.copy(geriAlinabilir = null) }
            // Ayni kimlik ve ayni tarih geri geldigi icin kayit listede
            // eski yerine oturuyor, en uste ziplamiyor.
            val yazmaHatasi = depo.kaydet(kayit)
            val sonuc = depo.kayitlariGetir()
            yaz(sonuc.copy(hata = yazmaHatasi ?: sonuc.hata))
        }
    }

    fun geriAlmaTuketildi() {
        _uiState.update { it.copy(geriAlinabilir = null) }
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
            hata = sonuc.hata,
            motorYok = sonuc.motorYok
        )
    }
}
