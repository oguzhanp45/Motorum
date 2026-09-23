package com.oguzhanp.motorum.feature.kayit.ekle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oguzhanp.motorum.data.hatirlatma.HatirlatmaZamanlayici
import com.oguzhanp.motorum.data.kayit.KayitDeposu
import com.oguzhanp.motorum.model.Kayit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class KayitEkleViewModel @Inject constructor(
    private val depo: KayitDeposu,
    private val zamanlayici: HatirlatmaZamanlayici,
    kayitliDurum: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(KayitEkleUiState())
    val uiState = _uiState.asStateFlow()

    // Kayit Detayi'ndaki "tekrarla"dan gelindiyse kopyalanacak kaydin kimligi.
    private val kopyaId: String? = kayitliDurum.get<String>("kopya")?.takeIf { it.isNotBlank() }
    private var kopyaUygulandi = false

    // Kopyadan acildiysa form zaten dolu: "son kayittan doldur" seridi gereksiz.
    val kopyadan: Boolean get() = kopyaId != null

    // Liste ana sayfanin ViewModel'inde; ekran onu verince kopya bir kez uygulaniyor.
    fun kopyaUygula(kayitlar: List<Kayit>) {
        if (kopyaUygulandi || kopyaId == null) return
        val kayit = kayitlar.firstOrNull { it.id == kopyaId } ?: return
        kopyaUygulandi = true
        _uiState.update { it.copy(form = kayittanForm(kayit)) }
    }

    fun guncelle(yeni: KayitEkleUiState) {
        _uiState.value = yeni
    }

    fun kaydet(kayit: Kayit) {
        viewModelScope.launch {
            _uiState.update { it.copy(kaydediliyor = true, hata = null) }
            val hata = depo.kaydet(kayit)
            // Alarm ancak kayit gercekten yazildiysa kuruluyor.
            if (hata == null) zamanlayici.esitle(kayit)
            _uiState.update {
                it.copy(kaydediliyor = false, hata = hata, basarili = hata == null)
            }
        }
    }
}
