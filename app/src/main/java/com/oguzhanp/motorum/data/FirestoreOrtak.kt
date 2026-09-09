package com.oguzhanp.motorum.data

import com.google.firebase.firestore.FirebaseFirestoreException

// Koleksiyon adlari tek yerde. Yolu her depo kendisi kuruyor ama isimler buradan
// geliyor: iki dosyada ayri ayri yazsaydik birini degistirmek digerini sessizce
// baska bir yola bakar hale getirirdi ve derleyici uyarmazdi.
internal const val KOLEKSIYON_KULLANICILAR = "users"
internal const val KOLEKSIYON_MOTORLAR = "motorlar"
internal const val KOLEKSIYON_KAYITLAR = "kayitlar"

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
