package com.oguzhanp.motorum.viewmodel

import androidx.lifecycle.ViewModel
import com.oguzhanp.motorum.model.Kayit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// liste + ekleme + toplamlar

data class KayıtUiState(
    val kayitlar: List<Kayit> = emptyList(),
    val isLoading: Boolean = false,
    val error: String = "",
    val toplamTutar: Double = 0.0,
    val toplamLitre: Double = 0.0
)

class KayitViewModel : ViewModel() {


    private val _uiState = MutableStateFlow(KayıtUiState())
    val uiState = _uiState.asStateFlow()


    //MutableStateFlow'un .value'suna dışarıdan yazılabilir.
    // Onu private yapıp dışarıya sadece okunabilir StateFlow versiyonunu açıyoruz.
    // Böylece bir UI dosyası kazara viewModel.kayitlar.value = yazamaz


    fun ekle(kayit: Kayit) {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                kayitlar = state.kayitlar + kayit,
                toplamTutar = state.toplamTutar + kayit.tutar,
                toplamLitre = state.toplamLitre + kayit.litre
            )
        }

        //yani en yeni kayıt listenin en üstünde görünür.
        //Sona istersen _kayitlar.value + kayit olmalı
    }


//    val toplamTutar: StateFlow<Double> = _kayitlar
//        .map { liste -> liste.sumOf { it.tutar } }
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)
//    // map = liste her değiştiğinde toplamı yeniden hesapla.
//    // stateIn = bu akışı StateFlow'a çevir çünkü Composeun her an okunabilecek şuaanki değere ihtiyacı var
//    // SharingStarted.WhileSubscribed(5_000) bu akış ne zaman çalışsın sorusunun cevabı: "onu dinleyen bir ekran varken çalış, son dinleyici gidince 5000 ms bekle,
//    // o süre içinde geri gelen olmazsa dur". Ekran döndüğünde ya da kısa süre arka plana atıldığında baştan hesaplama yapılmasını önler.
//    // 0.0 ise ilk değer — henüz hiçbir hesap yapılmamışken Compose'un okuyacağı başlangıç değeri.

//    val toplamLitre: StateFlow<Double> = _kayitlar
//        .map { liste -> liste.sumOf { it.litre } }
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

}

