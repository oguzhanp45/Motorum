package com.oguzhanp.motorum.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.oguzhanp.motorum.model.DilSecimi
import java.util.Locale

// Uygulama dili. Secimi DataStore'a yazmiyoruz: Android 13'ten beri sistemin
// kendi "uygulama dili" ozelligi var, alt surumlerde ayni isi appcompat
// goruyor. Boylece secim hem bizim Ayarlar ekranimizda hem de telefonun
// Ayarlar > Uygulamalar > Motorum > Dil ekraninda ayni yeri gosteriyor.
//
// Yazdigimiz anda Android ekrani yeniden kuruyor; ekran o sirada secimi
// buradan tekrar okuyor.
object DilAyari {

    fun oku(): DilSecimi {
        // toLanguageTags "tr-TR" ya da "en,tr" gibi donebiliyor: ilk etiketin
        // dil kismi bize yetiyor. Bos string "tercih yok" demek.
        val etiket = AppCompatDelegate.getApplicationLocales()
            .toLanguageTags()
            .substringBefore(',')
            .substringBefore('-')
        return DilSecimi.entries.firstOrNull { it.kod == etiket } ?: DilSecimi.SISTEM
    }

    // O an gecerli dilin kodu ("tr", "en"). Secim SISTEM ise telefonun dili.
    // Hava durumu servisine hangi dilde aciklama istedigimizi soylerken
    // kullaniliyor.
    fun etkinDilKodu(): String {
        val secilen = AppCompatDelegate.getApplicationLocales()
            .toLanguageTags()
            .substringBefore(',')
            .substringBefore('-')
        return secilen.ifBlank { Locale.getDefault().language }
    }

    fun yaz(secim: DilSecimi) {
        val liste = secim.kod
            ?.let { LocaleListCompat.forLanguageTags(it) }
        // Bos liste: "tercihimi sil, telefonun dilini kullan".
            ?: LocaleListCompat.getEmptyLocaleList()
        AppCompatDelegate.setApplicationLocales(liste)
    }
}
