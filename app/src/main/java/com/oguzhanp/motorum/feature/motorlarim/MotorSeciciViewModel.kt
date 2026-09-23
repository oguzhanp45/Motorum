package com.oguzhanp.motorum.feature.motorlarim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oguzhanp.motorum.data.motor.MotorDeposu
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Ust bardaki cip ve acilan panel icin. Motorlarim sayfasindan ayri bir
// ViewModel: ikisi farkli ekranda yasiyor, ayni ornegi paylasamazlar.
@HiltViewModel
class MotorSeciciViewModel @Inject constructor(
    private val depo: MotorDeposu
) : ViewModel() {

    private val _uiState = MutableStateFlow(MotorSeciciUiState())
    val uiState = _uiState.asStateFlow()

    fun yukle() {
        viewModelScope.launch {
            _uiState.update { it.copy(yukleniyor = true, hata = null) }
            val sonuc = depo.motorlariGetir()
            val seciliId = depo.seciliMotorId()
            _uiState.update {
                it.copy(
                    motorlar = sonuc.motorlar.sortedBy { motor -> motor.olusturmaMillis },
                    seciliMotor = sonuc.motorlar.firstOrNull { motor -> motor.id == seciliId },
                    yukleniyor = false,
                    // Deponun hatasi artik buraya kadar geliyor; eskiden
                    // yolda kayboluyordu.
                    hata = sonuc.hata
                )
            }
        }
    }

    fun panelAc() {
        _uiState.update { it.copy(panelAcik = true) }
    }

    fun panelKapat() {
        _uiState.update { it.copy(panelAcik = false) }
    }

    fun motoruSec(motorId: String) {
        // Zaten secili motora basildiysa paneli kapatmak yeterli: bosuna
        // yazma ve bosuna kayit cekme olmasin.
        if (motorId == _uiState.value.seciliMotor?.id) {
            panelKapat()
            return
        }
        viewModelScope.launch {
            depo.seciliMotoruDegistir(motorId)
            _uiState.update {
                it.copy(
                    seciliMotor = it.motorlar.firstOrNull { motor -> motor.id == motorId },
                    panelAcik = false,
                    secimTamam = true
                )
            }
        }
    }

    fun secimTuketildi() {
        _uiState.update { it.copy(secimTamam = false) }
    }
}
