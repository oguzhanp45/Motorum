package com.oguzhanp.motorum.ui.belge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oguzhanp.motorum.data.BelgeDeposu
import com.oguzhanp.motorum.data.HatirlatmaZamanlayici
import com.oguzhanp.motorum.model.Belge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BelgelerUiState(
    val belgeler: List<Belge> = emptyList(),
    val yukleniyor: Boolean = true,
    val hata: String? = null
)

// Istatistikler sayfasindaki Belgeler karti icin. Liste kartin kendi isi:
// ana sayfanin kayit listesiyle ilgisi yok, o yuzden ayri ViewModel.
@HiltViewModel
class BelgelerViewModel @Inject constructor(
    private val depo: BelgeDeposu,
    private val zamanlayici: HatirlatmaZamanlayici
) : ViewModel() {

    private val _uiState = MutableStateFlow(BelgelerUiState())
    val uiState = _uiState.asStateFlow()

    // Sayfa her one geldiginde cagriliyor: belge sayfasindan donunce liste taze olsun.
    fun yukle() {
        viewModelScope.launch {
            _uiState.update { it.copy(hata = null) }
            val sonuc = depo.belgeleriGetir()
            // "Suresi dolunca yenile" acik olanlarin tarihi gectiyse ileri
            // tasiniyor ve buluta yaziliyor. Yazma basarisiz olsa da ekranda
            // yeni tarih gorunuyor; bir sonraki acilista yeniden denenir.
            val belgeler = sonuc.belgeler.map { belge ->
                val yeni = belge.yenilenmisHali()
                if (yeni != belge) depo.kaydet(yeni)
                yeni
            }
            _uiState.value = BelgelerUiState(
                belgeler = belgeler,
                yukleniyor = false,
                // Motoru olmayan kullanici icin hata degil: kart bos gorunuyor.
                hata = sonuc.hata
            )
            // Liste bulutla ayniysa telefondaki bildirimler ona uyduruluyor:
            // eklenen, degisen, silinen ve yenilenen belgeler burada yerine oturuyor.
            if (sonuc.hata == null && !sonuc.motorYok) zamanlayici.belgeleriEsitle(belgeler)
        }
    }
}
