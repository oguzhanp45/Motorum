package com.oguzhanp.motorum.model

import androidx.annotation.StringRes
import com.oguzhanp.motorum.R
import java.util.Calendar
import java.util.UUID

// Takibi yapilan belge turleri. Ilk ucu motor basina bir tane olur: ikinci bir
// sigorta eklemek eskisinin yerine gecer. "Diger"in adini kullanici yaziyor
// (ehliyet, egzoz muayenesi...) ve istedigi kadar eklenebiliyor.
// etiket: veriyle giden ad (bildirim kaydi gibi), dilden bagimsiz.
// ad: ekranda gorunen adin kimligi; dile gore stringResource seciyor.
enum class BelgeTuru(val etiket: String, @StringRes val ad: Int, val tekil: Boolean) {
    SIGORTA("Sigorta", R.string.belge_turu_sigorta, true),
    MUAYENE("Muayene", R.string.belge_turu_muayene, true),
    KASKO("Kasko", R.string.belge_turu_kasko, true),
    DIGER("Diğer", R.string.belge_turu_diger, false)
}

// Harcama degil, bir bitis tarihi: toplamlara hic girmiyor. Motora bagli;
// iki motoru olanin iki ayri sigortasi var.
data class Belge(
    // Tekil turlerde kimlik turun kendisi ("SIGORTA"): ayni motora ikinci bir
    // sigorta yazmak eskisinin ustune yaziyor, cift kayit olusmuyor.
    val id: String = UUID.randomUUID().toString(),
    val tur: BelgeTuru,
    // Sadece "Diger"de dolu.
    val ad: String = "",
    val bitisMillis: Long,
    // Suresi dolunca tarih kac ay ileri tasinsin. null = yenileme kapali.
    // Sureyi kullanici seciyor: yasal sureler araca gore degisiyor, koda gommuyoruz.
    val yenileAy: Int? = null,
    // Bitisten kac gun once hatirlatilsin.
    val kacGunOnce: Int = 15
) {
    // Veriye giden ad (bildirim kaydi). Ekranda gorunen ad icin
    // ui/belge icindeki gorunenAdi() kullaniliyor: o dile gore seciyor.
    val gorunenAd: String get() = if (tur == BelgeTuru.DIGER) ad else tur.etiket

    // Bitise kalan takvim gunu. Bugun bitiyorsa 0, dun bittiyse -1.
    fun kalanGun(simdi: Long = System.currentTimeMillis()): Int =
        ((gunBasi(bitisMillis) - gunBasi(simdi)) / GUN_MS).toInt()

    // Yenileme aciksa ve tarih gectiyse, gelecege ulasana kadar sure kadar
    // ileri tasinmis hali. Bir yil boyunca uygulama acilmadiysa bile dogru yere
    // oturur. Degisiklik yoksa ayni nesne donuyor.
    fun yenilenmisHali(simdi: Long = System.currentTimeMillis()): Belge {
        val ay = yenileAy?.takeIf { it > 0 } ?: return this
        if (kalanGun(simdi) >= 0) return this
        var yeni = bitisMillis
        while (gunBasi(yeni) < gunBasi(simdi)) {
            yeni = Calendar.getInstance().apply { timeInMillis = yeni; add(Calendar.MONTH, ay) }.timeInMillis
        }
        return copy(bitisMillis = yeni)
    }
}

private const val GUN_MS = 24L * 60 * 60 * 1000

// Iki tarih de ayni saate (ogle) cekilip karsilastiriliyor: saat farki "kalan
// gun"u kaydirmasin. Gece yarisi degil ogle: yaz saati gecisindeki 23 ya da 25
// saatlik gunlerde bolme yine tam sayi versin.
private fun gunBasi(millis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = millis
    set(Calendar.HOUR_OF_DAY, 12)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis
