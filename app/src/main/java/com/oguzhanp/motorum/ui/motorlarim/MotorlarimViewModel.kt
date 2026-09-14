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
class MotorlarimViewModel @Inject constructor(
    private val depo: MotorDeposu
) : ViewModel() {

    private val _uiState = MutableStateFlow(MotorlarimUiState())
    val uiState = _uiState.asStateFlow()

    fun yukle() {
        viewModelScope.launch {
            _uiState.update { it.copy(yukleniyor = true, hata = null) }
            val sonuc = depo.motorlariGetir()
            _uiState.value = MotorlarimUiState(
                motorlar = sonuc.motorlar,
                // Secili motoru listeyi cektikten sonra soruyoruz: depo, kayitli
                // secim gecersizse en eskisine dusuyor ve dogru cevabi ancak
                // liste elindeyken verebiliyor.
                seciliMotorId = depo.seciliMotorId(),
                yukleniyor = false,
                hata = sonuc.hata
            )
        }
    }

    fun motoruSec(motorId: String) {
        viewModelScope.launch {
            depo.seciliMotoruDegistir(motorId)
            _uiState.update { it.copy(seciliMotorId = motorId, secimTamam = true) }
        }
    }

    fun secimTuketildi() {
        _uiState.update { it.copy(secimTamam = false) }
    }

    fun silmeOnayiAc(motor: Motor) {
        viewModelScope.launch {
            val sayi = depo.kayitSayisi(motor.id)
            _uiState.update { it.copy(silinecekMotor = motor, silinecekKayitSayisi = sayi) }
        }
    }

    fun silmeOnayiKapat() {
        _uiState.update { it.copy(silinecekMotor = null, silinecekKayitSayisi = 0) }
    }

    fun sil(motorId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(silinecekMotor = null, yukleniyor = true, hata = null)
            }
            val hata = depo.sil(motorId)
            if (hata != null) {
                _uiState.update { it.copy(yukleniyor = false, hata = hata) }
            } else {
                // Silinen motor secili olansa depo bir sonraki cozumlemede
                // kalanlarin en eskisine dusuyor; listeyi bastan cekmek yeterli.
                yukle()
            }
        }
    }
}
