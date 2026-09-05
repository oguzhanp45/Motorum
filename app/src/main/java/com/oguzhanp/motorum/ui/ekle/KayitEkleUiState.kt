package com.oguzhanp.motorum.ui.ekle

import com.oguzhanp.motorum.ui.form.KayitFormu


 //Ekleme ekraninin state'i.
 //Su an sadece formdan ibaret; ekrana ozel bir sey eklenirse buraya gelir.
 //nurayı kayit formu olarak ayirdik cunkü 2 ekranda aynı kontroller yapılıyor
 //oyüzden aynı kontoller kayit formu sayesinde 2 yerdede uzun uzun yazılmadi.
data class KayitEkleUiState(
    val form: KayitFormu = KayitFormu.Yakit()
)
