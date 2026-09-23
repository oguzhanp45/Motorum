package com.oguzhanp.motorum.ui.belge

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oguzhanp.motorum.data.BelgeDeposu
import com.oguzhanp.motorum.model.Belge
import com.oguzhanp.motorum.model.BelgeTuru
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// Hazir yenileme sureleri (ay). null = Ozel: kullanici kendisi yaziyor.
val YENILEME_SURELERI = listOf(12, 24)
val HATIRLATMA_GUNLERI = listOf(7, 15, 30)

// Formun ekrandaki hali. Belge'den ayri cunku yazilan metinler (ad, ozel ay)
// henuz dogrulanmamis olabilir.
data class BelgeFormu(
    val tur: BelgeTuru = BelgeTuru.SIGORTA,
    val ad: String = "",
    val bitisMillis: Long = System.currentTimeMillis(),
    val yenileAcik: Boolean = false,
    // Secili hazir sure; null = Ozel.
    val yenileAy: Int? = 12,
    val ozelAyYazi: String = "",
    val kacGunOnce: Int = 15,
    val adHatali: Boolean = false,
    val ozelAyHatali: Boolean = false
) {
    private val ozelAy: Int? get() = ozelAyYazi.toIntOrNull()?.takeIf { it in 1..120 }

    // Kaydedilecek yenileme suresi; kapaliysa null.
    val sonYenileAy: Int? get() = if (!yenileAcik) null else yenileAy ?: ozelAy

    fun dogrula(): BelgeFormu = copy(
        adHatali = tur == BelgeTuru.DIGER && ad.isBlank(),
        ozelAyHatali = yenileAcik && yenileAy == null && ozelAy == null
    )

    val gecerli: Boolean get() = !adHatali && !ozelAyHatali
}

data class BelgeUiState(
    val form: BelgeFormu = BelgeFormu(),
    // Duzenlenen belgenin kimligi; null = yeni belge.
    val duzenlenenId: String? = null,
    // Bu motorda kayitli tekil turler: "yerine gecer" notu icin.
    val kayitliTurler: Set<BelgeTuru> = emptySet(),
    val yukleniyor: Boolean = true,
    val calisiyor: Boolean = false,
    val silmeOnayi: Boolean = false,
    val hata: String? = null,
    val bitti: Boolean = false
)

@HiltViewModel
class BelgeViewModel @Inject constructor(
    private val depo: BelgeDeposu,
    kayitliDurum: SavedStateHandle
) : ViewModel() {

    // Rotadan gelenler: duzenlenecek belge ya da bos satirdan secilen tur.
    private val gelenId: String? = kayitliDurum.get<String>("id")?.takeIf { it.isNotBlank() }
    private val gelenTur: BelgeTuru? = kayitliDurum.get<String>("tur")
        ?.let { ad -> BelgeTuru.entries.firstOrNull { it.name == ad } }

    private val _uiState = MutableStateFlow(
        BelgeUiState(form = BelgeFormu(tur = gelenTur ?: BelgeTuru.SIGORTA), duzenlenenId = gelenId)
    )
    val uiState = _uiState.asStateFlow()

    init {
        // Liste zaten kucuk: tek belgeyi ayrica okumak yerine hepsini cekip
        // ariyoruz. Ayni cagri "bu turden kayitli var mi" sorusunu da cevapliyor.
        viewModelScope.launch {
            val sonuc = depo.belgeleriGetir()
            val mevcut = gelenId?.let { id -> sonuc.belgeler.firstOrNull { it.id == id } }
            _uiState.update { durum ->
                durum.copy(
                    form = mevcut?.let(::formaCevir) ?: durum.form,
                    kayitliTurler = sonuc.belgeler.map { it.tur }.filter { it.tekil }.toSet(),
                    yukleniyor = false,
                    hata = sonuc.hata
                )
            }
        }
    }

    fun formDegis(yeni: BelgeFormu) {
        _uiState.update { it.copy(form = yeni) }
    }

    fun silmeOnayiDegis(goster: Boolean) {
        _uiState.update { it.copy(silmeOnayi = goster) }
    }

    fun kaydet() {
        val kontrol = _uiState.value.form.dogrula()
        if (!kontrol.gecerli) {
            _uiState.update { it.copy(form = kontrol) }
            return
        }
        val eskiId = _uiState.value.duzenlenenId
        // Tekil turde kimlik turun adi: ayni motora ikinci sigorta yazilmiyor,
        // eskisinin yerine geciyor. "Diger" kendi kimligini koruyor.
        val yeniId = when {
            kontrol.tur.tekil -> kontrol.tur.name
            eskiId != null && eskiId !in BelgeTuru.entries.map { it.name } -> eskiId
            else -> UUID.randomUUID().toString()
        }
        val belge = Belge(
            id = yeniId,
            tur = kontrol.tur,
            ad = if (kontrol.tur == BelgeTuru.DIGER) kontrol.ad.trim() else "",
            bitisMillis = kontrol.bitisMillis,
            yenileAy = kontrol.sonYenileAy,
            kacGunOnce = kontrol.kacGunOnce
        )
        viewModelScope.launch {
            _uiState.update { it.copy(calisiyor = true, hata = null) }
            var hata = depo.kaydet(belge)
            // Tur degistiyse (Sigorta -> Muayene) eski kimlikteki belge kaldi:
            // yenisi yazildiktan sonra siliyoruz.
            if (hata == null && eskiId != null && eskiId != yeniId) hata = depo.sil(eskiId)
            _uiState.update { it.copy(calisiyor = false, hata = hata, bitti = hata == null) }
        }
    }

    fun sil() {
        val id = _uiState.value.duzenlenenId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(calisiyor = true, hata = null, silmeOnayi = false) }
            val hata = depo.sil(id)
            _uiState.update { it.copy(calisiyor = false, hata = hata, bitti = hata == null) }
        }
    }
}

private fun formaCevir(belge: Belge) = BelgeFormu(
    tur = belge.tur,
    ad = belge.ad,
    bitisMillis = belge.bitisMillis,
    yenileAcik = belge.yenileAy != null,
    yenileAy = belge.yenileAy?.takeIf { it in YENILEME_SURELERI } ?: if (belge.yenileAy == null) 12 else null,
    ozelAyYazi = belge.yenileAy?.takeIf { it !in YENILEME_SURELERI }?.toString().orEmpty(),
    kacGunOnce = belge.kacGunOnce
)
