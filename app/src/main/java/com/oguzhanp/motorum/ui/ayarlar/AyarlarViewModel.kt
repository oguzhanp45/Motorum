package com.oguzhanp.motorum.ui.ayarlar

import androidx.lifecycle.ViewModel
import com.oguzhanp.motorum.data.KimlikDeposu
import com.oguzhanp.motorum.data.MotorDeposu
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AyarlarViewModel @Inject constructor(
    private val kimlikDeposu: KimlikDeposu,
    private val motorDeposu: MotorDeposu
) : ViewModel() {

    // Once onbellek, sonra oturum. MotorDeposu cozdugu motor kimligini bellekte
    // tutuyor; temizlemezsek ayni hesapla tekrar girildiginde eski cozumleme
    // yeniden kullanilir.
    fun cikisYap() {
        motorDeposu.onbellegiTemizle()
        kimlikDeposu.cikisYap()
    }
}
