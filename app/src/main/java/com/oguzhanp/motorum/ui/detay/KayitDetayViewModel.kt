package com.oguzhanp.motorum.ui.detay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oguzhanp.motorum.data.KayitDeposu
import com.oguzhanp.motorum.model.Kayit
import com.oguzhanp.motorum.ui.form.KayitFormu
import com.oguzhanp.motorum.ui.form.TripNoktasiFormu
import com.oguzhanp.motorum.util.dakikaAl
import com.oguzhanp.motorum.util.saatAl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KayitDetayViewModel @Inject constructor(
    private val depo: KayitDeposu
) : ViewModel() {

    private val _uiState = MutableStateFlow(KayitDetayUiState())
    val uiState = _uiState.asStateFlow()

    private var baslatildi = false

    fun baslat(kayit: Kayit) {
        if (baslatildi) return
        baslatildi = true
        _uiState.value = KayitDetayUiState(form = formaCevir(kayit))
    }

    fun formDegis(yeni: KayitFormu) {
        _uiState.update { it.copy(form = yeni, hata = null) }
    }

    fun silmeOnayiDegis(goster: Boolean) {
        _uiState.update { it.copy(silmeOnayiGoster = goster) }
    }

    fun guncelle(kayit: Kayit) {
        viewModelScope.launch {
            _uiState.update { it.copy(calisiyor = true, hata = null) }
            val hata = depo.kaydet(kayit)
            _uiState.update {
                it.copy(calisiyor = false, hata = hata, bitti = hata == null)
            }
        }
    }

    fun sil(id: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(calisiyor = true, hata = null, silmeOnayiGoster = false)
            }
            val hata = depo.sil(id)
            _uiState.update {
                it.copy(calisiyor = false, hata = hata, bitti = hata == null)
            }
        }
    }
}

private fun formaCevir(kayit: Kayit): KayitFormu = when (kayit) {
    is Kayit.Yakit -> KayitFormu.Yakit(
        tarihMillis = kayit.tarihMillis,
        litreYazi = kayit.litre.toString(),
        tutarYazi = kayit.tutar.toString(),
        not = kayit.not
    )

    is Kayit.RoadTrip -> KayitFormu.RoadTrip(
        baslangic = TripNoktasiFormu(
            tarihMillis = kayit.baslangic.tarihMillis,
            saat = saatAl(kayit.baslangic.tarihMillis),
            dakika = dakikaAl(kayit.baslangic.tarihMillis),
            kmYazi = kayit.baslangic.km.toString(),
            sehir = kayit.baslangic.sehir
        ),
        bitis = kayit.bitis?.let {
            TripNoktasiFormu(
                tarihMillis = it.tarihMillis,
                saat = saatAl(it.tarihMillis),
                dakika = dakikaAl(it.tarihMillis),
                kmYazi = it.km.toString(),
                sehir = it.sehir
            )
        } ?: TripNoktasiFormu(),
        molalar = kayit.molalar,
        masrafYazi = if (kayit.tutar > 0.0) kayit.tutar.toString() else "",
        not = kayit.not
    )

    is Kayit.Bakim -> KayitFormu.Bakim(
        tarihMillis = kayit.tarihMillis,
        bakimTuru = kayit.bakimTuru,
        tutarYazi = kayit.tutar.toString(),
        not = kayit.not
    )

    is Kayit.Aksesuar -> KayitFormu.Aksesuar(
        tarihMillis = kayit.tarihMillis,
        aksesuarAdi = kayit.aksesuarAdi,
        tutarYazi = kayit.tutar.toString(),
        not = kayit.not
    )
}
