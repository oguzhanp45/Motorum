package com.oguzhanp.motorum.ui.detay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oguzhanp.motorum.data.HatirlatmaZamanlayici
import com.oguzhanp.motorum.data.KayitDeposu
import com.oguzhanp.motorum.model.Kayit
import com.oguzhanp.motorum.ui.form.KayitFormu
import com.oguzhanp.motorum.ui.form.TripNoktasiFormu
import com.oguzhanp.motorum.util.dakikaAl
import com.oguzhanp.motorum.util.saatAl
import com.oguzhanp.motorum.util.sayiyiYaziya
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KayitDetayViewModel @Inject constructor(
    private val depo: KayitDeposu,
    private val zamanlayici: HatirlatmaZamanlayici
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
            // esitle once iptal edip sonra kuruyor: hatirlatma degismis,
            // eklenmis ya da kaldirilmis olabilir.
            if (hata == null) zamanlayici.esitle(kayit)
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
            // Kayit gittiyse alarmi da iptal ediyoruz; yoksa olmayan bir
            // bakim icin bildirim calardi.
            if (hata == null) zamanlayici.iptal(id)
            _uiState.update {
                it.copy(calisiyor = false, hata = hata, bitti = hata == null)
            }
        }
    }
}

private fun formaCevir(kayit: Kayit): KayitFormu = when (kayit) {
    is Kayit.Yakit -> KayitFormu.Yakit(
        tarihMillis = kayit.tarihMillis,
        litreYazi = sayiyiYaziya(kayit.litre),
        tutarYazi = sayiyiYaziya(kayit.tutar),
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
        masrafYazi = if (kayit.tutar > 0.0) sayiyiYaziya(kayit.tutar) else "",
        not = kayit.not
    )

    is Kayit.Bakim -> KayitFormu.Bakim(
        tarihMillis = kayit.tarihMillis,
        bakimTuru = kayit.bakimTuru,
        tutarYazi = sayiyiYaziya(kayit.tutar),
        not = kayit.not,
        // Kayitli hatirlatma tek bir Long; form onu gun, saat ve dakika diye
        // uc parcada tuttugu icin burada geri ayriliyor.
        hatirlatmaAcik = kayit.hatirlatmaVar,
        hatirlatmaTarihMillis = kayit.hatirlatmaMillis ?: System.currentTimeMillis(),
        hatirlatmaSaat = kayit.hatirlatmaMillis?.let { saatAl(it) },
        hatirlatmaDakika = kayit.hatirlatmaMillis?.let { dakikaAl(it) }
    )

    is Kayit.Aksesuar -> KayitFormu.Aksesuar(
        tarihMillis = kayit.tarihMillis,
        aksesuarAdi = kayit.aksesuarAdi,
        tutarYazi = sayiyiYaziya(kayit.tutar),
        not = kayit.not
    )
}
