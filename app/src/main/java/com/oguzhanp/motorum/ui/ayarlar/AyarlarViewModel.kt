package com.oguzhanp.motorum.ui.ayarlar

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oguzhanp.motorum.data.AyarlarDeposu
import com.oguzhanp.motorum.data.CsvDisaAktarici
import com.oguzhanp.motorum.data.HavaDurumuOnbellegi
import com.oguzhanp.motorum.data.KayitDeposu
import com.oguzhanp.motorum.data.KimlikDeposu
import com.oguzhanp.motorum.data.MotorDeposu
import com.oguzhanp.motorum.model.Kayit
import com.oguzhanp.motorum.model.TemaSecimi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class AyarlarUiState(
    val tema: TemaSecimi = TemaSecimi.SISTEM,
    val eposta: String = "",
    // null = onbellekte hava yok (hic alinmamis ya da temizlenmis).
    val havaSonGuncelleme: Long? = null,
    // Kayitlar sunucudan cekilirken satirda donen gosterge cikiyor.
    val disaAktarimHazirlaniyor: Boolean = false,
    // Dolu oldugunda ekran "farkli kaydet" penceresini bu adla aciyor.
    val kaydedilecekDosyaAdi: String? = null,
    // Snackbar'da bir kez gosterilecek mesaj.
    val mesaj: AyarlarMesaji? = null
)

// Snackbar mesajlari metin olarak degil, "hangi mesaj" olarak tasiniyor:
// metni ekran seciyor, cunku dil secimi ekranin tarafinda. ViewModel'e
// Context verip getString cagirsaydik dil degisince eski dilde kalabilirdi.
sealed interface AyarlarMesaji {
    data object OnbellekTemizlendi : AyarlarMesaji
    data object MotorYok : AyarlarMesaji
    data object KayitYok : AyarlarMesaji
    data class Aktarildi(val adet: Int) : AyarlarMesaji
    // Depodan gelen hazir metin (su an Turkce): henuz cevrilmedi.
    data class Hata(val metin: String) : AyarlarMesaji
}

@HiltViewModel
class AyarlarViewModel @Inject constructor(
    private val kimlikDeposu: KimlikDeposu,
    private val motorDeposu: MotorDeposu,
    private val ayarlarDeposu: AyarlarDeposu,
    private val havaOnbellegi: HavaDurumuOnbellegi,
    private val kayitDeposu: KayitDeposu,
    private val csvDisaAktarici: CsvDisaAktarici
) : ViewModel() {

    private val _durum = MutableStateFlow(AyarlarUiState(eposta = kimlikDeposu.eposta))
    val durum: StateFlow<AyarlarUiState> = _durum.asStateFlow()

    // Dosya yeri secilirken bekleyen kayitlar. Once kayitlari cekiyoruz, sonra
    // pencereyi aciyoruz: kayit yoksa ya da internet yoksa bos dosya olusmasin.
    private var bekleyenKayitlar: List<Kayit> = emptyList()

    init {
        // Tema akis olarak dinleniyor: segment baska yerden degisse de dogru kalir.
        viewModelScope.launch {
            ayarlarDeposu.temaSecimi.collect { secim -> _durum.update { it.copy(tema = secim) } }
        }
        viewModelScope.launch {
            _durum.update { it.copy(havaSonGuncelleme = havaOnbellegi.sonGuncelleme()) }
        }
    }

    fun temaSec(secim: TemaSecimi) {
        viewModelScope.launch { ayarlarDeposu.temaSeciminiYaz(secim) }
    }

    fun havaOnbelleginiTemizle() {
        viewModelScope.launch {
            havaOnbellegi.temizle()
            _durum.update {
                it.copy(havaSonGuncelleme = null, mesaj = AyarlarMesaji.OnbellekTemizlendi)
            }
        }
    }

    fun disaAktarimaBasla() {
        if (_durum.value.disaAktarimHazirlaniyor) return
        _durum.update { it.copy(disaAktarimHazirlaniyor = true) }
        viewModelScope.launch {
            val sonuc = kayitDeposu.kayitlariGetir()
            val hata: AyarlarMesaji? = when {
                sonuc.motorYok -> AyarlarMesaji.MotorYok
                sonuc.hata != null -> AyarlarMesaji.Hata(sonuc.hata)
                sonuc.kayitlar.isEmpty() -> AyarlarMesaji.KayitYok
                else -> null
            }
            bekleyenKayitlar = if (hata == null) sonuc.kayitlar else emptyList()
            _durum.update {
                it.copy(
                    disaAktarimHazirlaniyor = false,
                    mesaj = hata,
                    kaydedilecekDosyaAdi = if (hata == null) dosyaAdi() else null
                )
            }
        }
    }

    // Pencere acildi: istek tuketildi, bir daha acilmasin.
    fun kaydetmePenceresiAcildi() {
        _durum.update { it.copy(kaydedilecekDosyaAdi = null) }
    }

    // hedef null = kullanici pencereyi kapatti; sessizce vazgeciyoruz.
    fun dosyaSecildi(hedef: Uri?) {
        val kayitlar = bekleyenKayitlar
        bekleyenKayitlar = emptyList()
        if (hedef == null || kayitlar.isEmpty()) return
        viewModelScope.launch {
            val hata = csvDisaAktarici.yaz(hedef, kayitlar)
            _durum.update {
                it.copy(
                    mesaj = hata?.let(AyarlarMesaji::Hata) ?: AyarlarMesaji.Aktarildi(kayitlar.size)
                )
            }
        }
    }

    fun mesajGosterildi() {
        _durum.update { it.copy(mesaj = null) }
    }

    // Once onbellek, sonra oturum. MotorDeposu cozdugu motor kimligini bellekte
    // tutuyor; temizlemezsek ayni hesapla tekrar girildiginde eski cozumleme
    // yeniden kullanilir.
    fun cikisYap() {
        motorDeposu.onbellegiTemizle()
        kimlikDeposu.cikisYap()
    }

    // Tarihli ad: ayni gun ikinci kez aktarilirsa sistem "(1)" ekliyor, ustune yazmiyor.
    private fun dosyaAdi(): String =
        "motorum-kayitlar-" + SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date()) + ".csv"
}
