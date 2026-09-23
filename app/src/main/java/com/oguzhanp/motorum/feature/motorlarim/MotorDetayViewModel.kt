package com.oguzhanp.motorum.feature.motorlarim

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oguzhanp.motorum.data.motor.FotografIslemi
import com.oguzhanp.motorum.data.motor.GorselHazirlayici
import com.oguzhanp.motorum.data.motor.MotorDeposu
import com.oguzhanp.motorum.model.Motor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MotorDetayViewModel @Inject constructor(
    private val depo: MotorDeposu,
    private val gorselHazirlayici: GorselHazirlayici
) : ViewModel() {

    private val _uiState = MutableStateFlow(MotorDetayUiState())
    val uiState = _uiState.asStateFlow()

    // Duzenlemede mevcut motoru sakliyoruz: kaydederken kimligi ve olusturma
    // tarihi korunsun, formda olmayan alanlar kaybolmasin.
    private var mevcutMotor: Motor? = null
    private var baslatildi = false

    // Panelden gelindiyse true: kaydedilen motor ayni anda secili motor oluyor.
    private var kaydettiktenSonraSec = false

    // Fotografla ilgili bekleyen degisiklik. Ekranda gosterilen goruntuden ayri
    // duruyor cunku bunlar kaydederken yazilacak ham veriler.
    private var fotografIslemi: FotografIslemi = FotografIslemi.Dokunma
    private var yeniOnizleme: String? = null

    fun baslat(motorId: String?, secilsin: Boolean = false) {
        if (baslatildi) return
        baslatildi = true
        kaydettiktenSonraSec = secilsin

        if (motorId == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(calisiyor = true) }
            val sonuc = depo.motorlariGetir()
            val motor = sonuc.motorlar.firstOrNull { it.id == motorId }
            mevcutMotor = motor
            _uiState.value = MotorDetayUiState(
                form = motor?.formaCevir() ?: MotorFormu(),
                yeniMi = motor == null,
                hata = sonuc.hata
            )
            if (motor != null) fotografiYukle(motor)
        }
    }

    // Once elimizdeki onizlemeyi gosteriyoruz ki ekran bos kalmasin, tam boy
    // sunucudan gelince onun yerini aliyor. Tam boy gelmezse onizleme ekranda
    // kaliyor ve hata gostermiyoruz: kullanici zaten bir gorsel goruyor.
    private suspend fun fotografiYukle(motor: Motor) {
        if (!motor.fotografVar) return

        gorseliYaz(motor.onizleme)
        _uiState.update { it.copy(fotografCalisiyor = true) }
        val sonuc = depo.fotografGetir(motor.id)
        sonuc.veri?.let { gorseliYaz(it) }
        _uiState.update { it.copy(fotografCalisiyor = false) }
    }

    private suspend fun gorseliYaz(base64: String) {
        val gorsel = withContext(Dispatchers.Default) { base64Coz(base64) }
        if (gorsel != null) _uiState.update { it.copy(fotograf = gorsel) }
    }

    fun fotografSec(uri: Uri) {
        viewModelScope.launch { fotografiIsle(uri) }
    }

    // Kameranin yazdigi dosyayi kullaniyoruz ve isimiz bitince siliyoruz.
    fun fotografCekildi(uri: Uri) {
        viewModelScope.launch {
            fotografiIsle(uri)
            gorselHazirlayici.geciciSil(uri)
        }
    }

    fun kameraHedefi(): Uri? = gorselHazirlayici.kameraHedefi()

    fun kameraAcilamadi() {
        _uiState.update { it.copy(hata = "Kamera açılamadı") }
    }

    private suspend fun fotografiIsle(uri: Uri) {
        _uiState.update { it.copy(fotografCalisiyor = true, hata = null) }

        val hazir = gorselHazirlayici.hazirla(uri)
        if (hazir == null) {
            _uiState.update {
                it.copy(fotografCalisiyor = false, hata = "Fotoğraf okunamadı")
            }
            return
        }

        // Henuz sunucuya bir sey yazilmiyor: fotograf da form alanlari gibi
        // Kaydet'e basilinca gecerli oluyor.
        fotografIslemi = FotografIslemi.Degistir(hazir.tamBoy)
        yeniOnizleme = hazir.onizleme
        gorseliYaz(hazir.tamBoy)
        _uiState.update { it.copy(fotografCalisiyor = false) }
    }

    fun fotografKaldir() {
        fotografIslemi = FotografIslemi.Kaldir
        yeniOnizleme = ""
        _uiState.update { it.copy(fotograf = null) }
    }

    fun formDegis(yeni: MotorFormu) {
        _uiState.update { it.copy(form = yeni, hata = null) }
    }

    fun kaydet() {
        val kontrol = _uiState.value.form.dogrula()
        if (!kontrol.gecerli) {
            _uiState.update { it.copy(form = kontrol) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(calisiyor = true, hata = null) }

            // copy ile kimlik korunuyor; yeni motorda Motor() kendi kimligini uretiyor.
            val motor = mevcutMotor?.let { eski ->
                eski.copy(
                    marka = kontrol.marka.trim(),
                    model = kontrol.model.trim(),
                    plaka = kontrol.plaka.trim(),
                    onizleme = yeniOnizleme ?: eski.onizleme
                )
            } ?: Motor(
                marka = kontrol.marka.trim(),
                model = kontrol.model.trim(),
                plaka = kontrol.plaka.trim(),
                onizleme = yeniOnizleme.orEmpty()
            )

            val hata = depo.kaydet(motor, fotografIslemi)
            if (hata == null && kaydettiktenSonraSec) {
                depo.seciliMotoruDegistir(motor.id)
            }
            _uiState.update {
                it.copy(calisiyor = false, hata = hata, bitti = hata == null)
            }
        }
    }
}
