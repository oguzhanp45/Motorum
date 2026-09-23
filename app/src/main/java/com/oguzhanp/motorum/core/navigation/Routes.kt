package com.oguzhanp.motorum.core.navigation

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
    // Ayni ekranin istege bagli parametreli kalibi. Parametresiz "kayit_ekle"
    // de buna uyuyor, FAB eskisi gibi calisiyor. kopya: Kayit Detayi'ndaki
    // "tekrarla"dan gelen kaydin kimligi; form onunla dolu aciliyor.
    const val KAYIT_EKLE_KALIBI = "kayit_ekle?kopya={kopya}"
    const val KAYIT_DETAY = "kayit_detay/{id}"
    // Ana bolgenin disinda duruyor: alt bar olmayan, geri oklu tam sayfa.
    // Grafin icine koysaydik alt bar da cizilirdi.
    const val ISTATISTIK = "istatistik"
    // Belge ekleme ve duzenleme ayni sayfa. id yoksa yeni belge; tur verilirse
    // (kartta bos satira dokunuldu) o tur secili aciliyor.
    const val BELGE = "belge?id={id}&tur={tur}"
    // Ayarlar'dan acilan belge listesi. Istatistikler gibi alt barsiz tam sayfa.
    const val BELGELER = "belgeler"
    fun motorEkleRotasi(secilsin: Boolean = false) = "motor_ekle?sec=$secilsin"
    fun kayitKopyaRotasi(id: String) = "kayit_ekle?kopya=$id"
    fun belgeRotasi(id: String? = null, tur: String? = null) = "belge?id=${id ?: ""}&tur=${tur ?: ""}"

    //Routes.KAYIT_DETAY = "kayit_detay/{id}" —
    // süslü parantez Navigation'a "burada bir parametre var" der.
    // Gitmek için navigate("kayit_detay/$id")
    //okumak için backStackEntry.arguments?.getString("id")
    // Bu üç yerdeki id metni birbirini tutmak zorunda
}
