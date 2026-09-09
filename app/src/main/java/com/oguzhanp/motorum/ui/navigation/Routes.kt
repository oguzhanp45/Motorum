package com.oguzhanp.motorum.ui.navigation

object Routes {
    const val ONBOARDING = "onboarding"
    const val GIRIS = "giris"
    const val UYE_OL = "uye_ol"

    // Uc sekmeyi saran ic graf. Sekme gecislerinde popUpTo icin sabit bir
    // baslangic noktasi gerekiyor; grafin kendi baslangici degiskken oldugu icin
    // (onboarding / giris / ana sayfa) sekmeler kendi grafinda toplandi.
    const val ANA_BOLGE = "ana_bolge"
    const val ANA_SAYFA = "ana_sayfa"
    const val MOTORLARIM = "motorlarim"
    const val AYARLAR = "ayarlar"
    // Soru isaretli kisim istege bagli parametre: verilmezse varsayilan
    // kullaniliyor. sec=true panelden gelindigini soyluyor, o zaman kaydedilen
    // motor otomatik seciliyor.
    const val MOTOR_EKLE = "motor_ekle?sec={sec}"
    const val MOTOR_DETAY = "motor_detay/{id}"
    const val KAYIT_EKLE = "kayit_ekle"
    const val KAYIT_DETAY = "kayit_detay/{id}"
    fun motorEkleRotasi(secilsin: Boolean = false) = "motor_ekle?sec=$secilsin"

    //Routes.KAYIT_DETAY = "kayit_detay/{id}" —
    // süslü parantez Navigation'a "burada bir parametre var" der.
    // Gitmek için navigate("kayit_detay/$id")
    //okumak için backStackEntry.arguments?.getString("id")
    // Bu üç yerdeki id metni birbirini tutmak zorunda
}
