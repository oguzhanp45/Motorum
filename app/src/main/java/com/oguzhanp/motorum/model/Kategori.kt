package com.oguzhanp.motorum.model

import androidx.annotation.StringRes
import com.oguzhanp.motorum.R

// Liste sabit olduğu için enum: yeni bir kategori eklendiğinde derleyici
// bütün when bloklarını eksik dal olarak gösteriyor.
// etiket: disa aktarilan CSV'ye yazilan ad. Dilden bagimsiz, hep ayni:
// dosyayi Excel'de acan ya da eski dosyayla karsilastiran kisi icin sutun
// degerleri degismemeli.
// ad: ekranda gorunen adin kimligi; dile gore stringResource seciyor.
enum class Kategori(val etiket: String, @StringRes val ad: Int) {
    YAKIT("Yakıt", R.string.kategori_yakit),
    ROAD_TRIP("Road Trip", R.string.kategori_road_trip),
    BAKIM("Bakım", R.string.kategori_bakim),
    AKSESUAR("Aksesuar", R.string.kategori_aksesuar)
}
