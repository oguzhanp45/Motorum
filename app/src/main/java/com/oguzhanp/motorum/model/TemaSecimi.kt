package com.oguzhanp.motorum.model

import androidx.annotation.StringRes
import com.oguzhanp.motorum.R

// Kullanicinin tema tercihi. SISTEM telefonun ayarini izliyor; digerleri
// telefon ne derse desin sabit kaliyor.
//
// etiket metnin kendisi degil kimligi: ekranda hangi dilde yazilacagina
// stringResource karar veriyor.
enum class TemaSecimi(@StringRes val etiket: Int) {
    ACIK(R.string.tema_acik),
    KARANLIK(R.string.tema_karanlik),
    SISTEM(R.string.secim_sistem)
}
