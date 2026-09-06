package com.oguzhanp.motorum.ui.home

import androidx.lifecycle.ViewModel
import com.oguzhanp.motorum.model.Kayit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// liste + ekleme + toplamlar

// Toplamlar uc fonksiyonda da ayni sekilde yeniden hesaplaniyor.
// Ortak bir yardimciya cikarilmadi: her fonksiyon tek basina okunabilsin diye.
class KayitViewModel : ViewModel() {


    private val _uiState = MutableStateFlow(KayitUiState())
    val uiState = _uiState.asStateFlow()


    //MutableStateFlow'un .value'suna dışarıdan yazılabilir.
    // Onu private yapıp dışarıya sadece okunabilir StateFlow versiyonunu açıyoruz.
    // Böylece bir UI dosyası kazara viewModel.kayitlar.value = yazamaz


    fun ekle(kayit: Kayit) {
        _uiState.update {
            val yeniListe = it.kayitlar + kayit
            it.copy(
                kayitlar = yeniListe,
                // Toplamlar her zaman listeden turer. Birikimli toplasaydik
                // silme ve duzenleme geldiginde toplam listeyle uyusmazdi.
                toplamTutar = yeniListe.sumOf { k -> k.tutar },
                // litre sadece yakit kayitlarinda var, once o tipe suzuluyor.
                toplamLitre = yeniListe.filterIsInstance<Kayit.Yakit>().sumOf { k -> k.litre },
                // mesafe devam eden yolculukta 0 donduruyor, toplama etkisi yok.
                toplamKm = yeniListe.filterIsInstance<Kayit.RoadTrip>().sumOf { k -> k.mesafe }
            )
        }
    }

    fun duzenle(kayit: Kayit) {
        _uiState.update {
            val yeniListe = it.kayitlar.map { mevcut ->
                if (mevcut.id == kayit.id) kayit else mevcut
            }
            it.copy(
                kayitlar = yeniListe,
                toplamTutar = yeniListe.sumOf { k -> k.tutar },
                toplamLitre = yeniListe.filterIsInstance<Kayit.Yakit>().sumOf { k -> k.litre },
                toplamKm = yeniListe.filterIsInstance<Kayit.RoadTrip>().sumOf { k -> k.mesafe }
            )
        }
    }
    //map ile listeyi gezip aynı id'li kaydı yenisiyle değiştiriyor, diğerlerine dokunmuyor.
    // Toplamlar sumOf ile yeniden hesaplanıyor;
    // birikimli bıraksaydık düzenlemede eski değer de yeni değer de sayılırdı

    fun sil(id: String) { // Silmek icin tam kayit gerekmiyor, "hangisi" bilgisi yetiyor.
        _uiState.update {
            val yeniListe = it.kayitlar.filter { mevcut -> mevcut.id != id }
            it.copy(
                kayitlar = yeniListe,
                toplamTutar = yeniListe.sumOf { k -> k.tutar },
                toplamLitre = yeniListe.filterIsInstance<Kayit.Yakit>().sumOf { k -> k.litre },
                toplamKm = yeniListe.filterIsInstance<Kayit.RoadTrip>().sumOf { k -> k.mesafe }
            )
        }
    }
    // filter id'si tutmayanlari gecirir, tutani disarida birakir.
    // Yeni liste uretilir; ekle/duzenle ile ayni degismez (immutable) kalip.


    // Listeyi tutan yer burasi oldugu icin degistiren fonksiyonlar da burada.
    // Ileride veri katmani (KayitDeposu) eklenirse ekle/duzenle/sil oraya tasinacak;
    // ekran ViewModel'leri listeyi degil depoyu cagirir ileride yapilabilir.
}
