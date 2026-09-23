package com.oguzhanp.motorum.data.ortak

import com.google.firebase.firestore.FirebaseFirestoreException

// Koleksiyon adlari tek yerde. Yolu her depo kendisi kuruyor ama isimler buradan
// geliyor: iki dosyada ayri ayri yazsaydik birini degistirmek digerini sessizce
// baska bir yola bakar hale getirirdi ve derleyici uyarmazdi.
internal const val KOLEKSIYON_KULLANICILAR = "users"
internal const val KOLEKSIYON_MOTORLAR = "motorlar"
internal const val KOLEKSIYON_KAYITLAR = "kayitlar"
internal const val KOLEKSIYON_BELGELER = "belgeler"

// Motorun tam boy fotografi. Motor basina tek fotograf oldugu icin belge
// kimligi de sabit: rastgele kimlik uretip saklamaya gerek yok, yolu biliyoruz.
internal const val KOLEKSIYON_MEDYA = "medya"
internal const val BELGE_FOTOGRAF = "fotograf"
internal const val ALAN_VERI = "veri"

internal const val OTURUM_YOK = "Oturum açık değil"
internal const val INTERNET_YOK = "İnternet bağlantısı yok"

internal fun hataMesaji(hata: Exception): String = when {
    hata is FirebaseFirestoreException &&
            hata.code == FirebaseFirestoreException.Code.UNAVAILABLE -> INTERNET_YOK

    hata is FirebaseFirestoreException &&
            hata.code == FirebaseFirestoreException.Code.PERMISSION_DENIED ->
        "Bu veriye erişim izniniz yok"

    else -> "Bir sorun oluştu, tekrar deneyin"
}
