package com.oguzhanp.motorum.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oguzhanp.motorum.data.HatirlatmaZamanlayici
import com.oguzhanp.motorum.data.KayitDeposu
import com.oguzhanp.motorum.data.KayitSonucu
import com.oguzhanp.motorum.data.MotorDeposu
import com.oguzhanp.motorum.model.Kayit
import com.oguzhanp.motorum.util.gidilenYolHesapla
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KayitViewModel @Inject constructor(
    private val depo: KayitDeposu,
    private val zamanlayici: HatirlatmaZamanlayici,
    private val motorDeposu: MotorDeposu
) : ViewModel() {

    private val _uiState = MutableStateFlow(KayitUiState())
    val uiState = _uiState.asStateFlow()

    //MutableStateFlow'un .value'suna dışarıdan yazılabilir.
    // Onu private yapıp dışarıya sadece okunabilir StateFlow versiyonunu açıyoruz.
    // Böylece bir UI dosyası kazara viewModel.kayitlar.value = yazamaz

    // temizle: motor degistiginde true geliyor. Eldeki liste artik baska bir
    // motora ait, ekranda birakmak yanlis bilgi gostermek olurdu; bosaltiyoruz
    // ki yerine dairesi ciksin. Normal acilislarda liste yerinde kaliyor.
    fun yukle(temizle: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                if (temizle) it.copy(kayitlar = emptyList(), yukleniyor = true, hata = null)
                else it.copy(yukleniyor = true, hata = null)
            }
            yukleVeEsitle()
        }
    }

    // Asagi cekince cagriliyor. yukle() ile ayni isi yapiyor, tek farki hangi
    // bayragi kaldirdigi: liste ekranda kalsin diye.
    fun yenile() {
        viewModelScope.launch {
            _uiState.update { it.copy(yenileniyor = true, hata = null) }
            yukleVeEsitle()
        }
    }

    // Kaydirinca kayit gercekten siliniyor, ertelenmiyor. Geri Al ayni kaydi
    // ayni kimlikle tekrar yaziyor. yukleniyor bilerek kaldirilmadi: satir
    // zaten gitti, ustune tum listeyi daireyle degistirmek gereksiz.
    fun sil(id: String) {
        viewModelScope.launch {
            val silinen = _uiState.value.kayitlar.firstOrNull { it.id == id } ?: return@launch

            // Satiri once ekrandan kaldiriyoruz. Bu bir basari iddiasi degil,
            // kaydirma hareketinin karsiligi.
            yaz(KayitSonucu(kayitlar = _uiState.value.kayitlar.filterNot { it.id == id }))

            // Geri Al teklifi HEMEN veriliyor. Onceden teklif sunucudan cevap
            // gelip liste bastan cekildikten sonra kuruluyordu: satirin gitmesi
            // ile snackbar arasinda saniyeler oluyor, cevap gecikince ya da
            // liste cekilirken hata donunce teklif hic gorunmuyordu.
            _uiState.update { it.copy(geriAlinabilir = silinen) }

            val silmeHatasi = depo.sil(id)
            // Kayit gittiyse alarmi da iptal ediyoruz.
            if (silmeHatasi == null) zamanlayici.iptal(id)

            val sonuc = depo.kayitlariGetir()
            yaz(sonuc.copy(hata = silmeHatasi ?: sonuc.hata))

            // yaz() yeni bir durum nesnesi kuruyor, teklifi elle geri koyuyoruz.
            // Silme basarisizsa teklif de anlamsiz: kayit zaten geri gelecek.
            _uiState.update { it.copy(geriAlinabilir = if (silmeHatasi == null) silinen else null) }
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
            // Kayit geri geldiyse hatirlatmasi da geri gelmeli.
            if (yazmaHatasi == null) zamanlayici.esitle(kayit)

            val sonuc = depo.kayitlariGetir()
            yaz(sonuc.copy(hata = yazmaHatasi ?: sonuc.hata))
        }
    }

    // ------------------------------------------------ HATIRLATMA PANELI

    fun paneliAc(kayitId: String) {
        _uiState.update { it.copy(panelKayitId = kayitId) }
    }

    fun paneliKapat() {
        _uiState.update { it.copy(panelKayitId = null) }
    }

    // Bildirimden gelindi. Bildirim baska bir motora aitse once o motor
    // seciliyor ve listesi cekiliyor; panel ancak kayit listedeyken acilabilir.
    fun hatirlatmaPaneliniAc(kayitId: String, motorId: String) {
        viewModelScope.launch {
            if (motorDeposu.seciliMotorId() != motorId) {
                motorDeposu.seciliMotoruDegistir(motorId)
                _uiState.update { it.copy(kayitlar = emptyList(), yukleniyor = true, hata = null) }
                yukleVeEsitle()
            }
            _uiState.update { it.copy(panelKayitId = kayitId) }
        }
    }

    // Paneldeki uc dugme. Uc durumda da kaydin tamami yeniden yaziliyor ve
    // alarm ona gore esitleniyor: esitle iptal edip gerekiyorsa yeniden kuruyor.
    fun hatirlatmaYaptirdim(kayit: Kayit.Bakim) =
        hatirlatmayiDegistir(kayit.copy(hatirlatmaYapildiMillis = System.currentTimeMillis()))

    fun hatirlatmaErtele(kayit: Kayit.Bakim) =
        hatirlatmayiDegistir(kayit.copy(hatirlatmaMillis = System.currentTimeMillis() + BIR_HAFTA_MS))

    // "Vazgectim": hatirlatma tamamen kalkiyor, cip de kayboluyor.
    // Yaptirdim'dan farki: kayitta "yapildi" izi kalmiyor.
    fun hatirlatmayiKapat(kayit: Kayit.Bakim) =
        hatirlatmayiDegistir(kayit.copy(hatirlatmaMillis = null, hatirlatmaYapildiMillis = null))

    private fun hatirlatmayiDegistir(yeni: Kayit.Bakim) {
        viewModelScope.launch {
            _uiState.update { it.copy(panelKayitId = null) }
            val hata = depo.kaydet(yeni)
            if (hata == null) zamanlayici.esitle(yeni)
            val sonuc = depo.kayitlariGetir()
            yaz(sonuc.copy(hata = hata ?: sonuc.hata))
        }
    }

    fun geriAlmaTuketildi() {
        _uiState.update { it.copy(geriAlinabilir = null) }
    }

    // Kayitlar gelince telefondaki alarmlar buluta uyduruluyor: yeniden yukleme,
    // yeni telefon ya da baska yerde degisen hatirlatmalar boylece yerine oturuyor.
    // Liste ekrana once yaziliyor; esitleme arkada, ekrani bekletmiyor.
    private suspend fun yukleVeEsitle() {
        val sonuc = depo.kayitlariGetir()
        yaz(sonuc)
        if (sonuc.hata == null && !sonuc.motorYok) zamanlayici.buluttanEsitle(sonuc.kayitlar)
    }

    private fun yaz(sonuc: KayitSonucu) {
        _uiState.value = KayitUiState(
            kayitlar = sonuc.kayitlar,
            // Toplamlar her zaman listeden turer.
            toplamTutar = sonuc.kayitlar.sumOf { it.tutar },
            // litre sadece yakit kayitlarinda var, once o tipe suzuluyor.
            toplamLitre = sonuc.kayitlar.filterIsInstance<Kayit.Yakit>().sumOf { it.litre },
            // Yol artik gezi mesafelerinin toplami degil: butun kayitlardaki
            // sayac okumalarinin en buyugu ile en kucugunun farki. Boylece gezi
            // kaydi acmadan gidilen kilometre de sayiliyor.
            gidilenYol = gidilenYolHesapla(sonuc.kayitlar),
            yukleniyor = false,
            hata = sonuc.hata,
            motorYok = sonuc.motorYok,
            // Asagi cekip yenilerken acik panel kapanmasin.
            panelKayitId = _uiState.value.panelKayitId
        )
    }
}

private const val BIR_HAFTA_MS = 7L * 24 * 60 * 60 * 1000
