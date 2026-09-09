package com.oguzhanp.motorum.ui.motorlarim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oguzhanp.motorum.data.MotorDeposu
import com.oguzhanp.motorum.model.Motor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MotorDetayViewModel @Inject constructor(
    private val depo: MotorDeposu
) : ViewModel() {

    private val _uiState = MutableStateFlow(MotorDetayUiState())
    val uiState = _uiState.asStateFlow()

    // Duzenlemede mevcut motoru sakliyoruz: kaydederken kimligi ve olusturma
    // tarihi korunsun, formda olmayan alanlar kaybolmasin.
    private var mevcutMotor: Motor? = null
    private var baslatildi = false

    // Panelden gelindiyse true: kaydedilen motor ayni anda secili motor oluyor.
    private var kaydettiktenSonraSec = false

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
        }
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
            val motor = mevcutMotor?.copy(
                marka = kontrol.marka.trim(),
                model = kontrol.model.trim(),
                plaka = kontrol.plaka.trim()
            ) ?: Motor(
                marka = kontrol.marka.trim(),
                model = kontrol.model.trim(),
                plaka = kontrol.plaka.trim()
            )

            val hata = depo.kaydet(motor)
            if (hata == null && kaydettiktenSonraSec) {
                depo.seciliMotoruDegistir(motor.id)
            }
            _uiState.update {
                it.copy(calisiyor = false, hata = hata, bitti = hata == null)
            }
        }
    }
}
