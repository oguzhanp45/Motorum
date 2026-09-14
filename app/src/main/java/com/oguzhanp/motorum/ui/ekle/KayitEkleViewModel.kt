package com.oguzhanp.motorum.ui.ekle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oguzhanp.motorum.data.HatirlatmaZamanlayici
import com.oguzhanp.motorum.data.KayitDeposu
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
    private val zamanlayici: HatirlatmaZamanlayici
) : ViewModel() {

    private val _uiState = MutableStateFlow(KayitEkleUiState())
    val uiState = _uiState.asStateFlow()

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
