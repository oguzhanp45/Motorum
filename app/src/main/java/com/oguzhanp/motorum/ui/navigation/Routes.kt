package com.oguzhanp.motorum.ui.navigation

object Routes {
    const val ANA_SAYFA = "ana_sayfa"
    const val KAYIT_EKLE = "kayit_ekle"
    const val KAYIT_DETAY = "kayit_detay/{id}"
    //Routes.KAYIT_DETAY = "kayit_detay/{id}" —
    // süslü parantez Navigation'a "burada bir parametre var" der.
    // Gitmek için navigate("kayit_detay/$id")
    //okumak için backStackEntry.arguments?.getString("id")
    // Bu üç yerdeki id metni birbirini tutmak zorunda
}
