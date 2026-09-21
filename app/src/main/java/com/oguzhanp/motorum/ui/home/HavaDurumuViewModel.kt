package com.oguzhanp.motorum.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oguzhanp.motorum.data.HavaDurumuDeposu
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HavaDurumuViewModel @Inject constructor(
    private val depo: HavaDurumuDeposu
) : ViewModel() {

    private val _hal = MutableStateFlow<HavaDurumuHali>(HavaDurumuHali.Yukleniyor)
    val hal = _hal.asStateFlow()

    // Ust uste istek gitmesin diye. Listeyi hizlica birkac kez asagi cekince
    // her cekis ayri bir konum olcumu baslatiyordu.
    private var calisiyor = false

    fun yukle(zorla: Boolean = false) {
        if (calisiyor) return
        calisiyor = true
        viewModelScope.launch {
            try {
                // Elde gosterilecek veri varken karti daireyle degistirmiyoruz:
                // yenileme sirasinda eski deger ekranda kalsin, PullToRefresh
                // zaten kendi gostergesini ciziyor.
                if (_hal.value !is HavaDurumuHali.Hazir) {
                    _hal.value = HavaDurumuHali.Yukleniyor
                }
                val sonuc = depo.getir(zorla)
                _hal.value = sonuc.hava
                    ?.let { HavaDurumuHali.Hazir(it) }
                    ?: HavaDurumuHali.Alinamadi
            } finally {
                // finally sart: is iptal olsa bile bayrak acik kalmasin,
                // yoksa bir daha hic yukleme yapamazdik.
                calisiyor = false
            }
        }
    }

    // Ekran izin durumunu bildiriyor: acilista (istendiMi = false) ya da
    // kullanici karttaki butona basip izin vermediginde (istendiMi = true).
    fun izinYok(istendiMi: Boolean) {
        _hal.value = HavaDurumuHali.IzinYok(izinIstendi = istendiMi)
    }
}
