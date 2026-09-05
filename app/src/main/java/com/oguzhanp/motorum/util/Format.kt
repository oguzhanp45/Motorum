package com.oguzhanp.motorum.util
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Locale("tr") yapicisi kullanimdan kaldirildi: verdigin metni hic dogrulamiyordu.
// forLanguageTag dil etiketini standarda gore okuyor.
// Tek yerde durup her fonksiyonda kullaniliyor, bes tekrar yerine bir tanim.
private val TR: Locale = Locale.forLanguageTag("tr")

fun formatTarih(millis: Long): String =
    SimpleDateFormat("dd.MM.yyyy", TR).format(Date(millis))

//formatTl: %.2f = "ondalıklı sayıyı 2 basamakla yaz".
//%.0f yaparsan kuruş görünmez
//tarihMillis). Date(millis) bu sayıyı tarih nesnesine çevirir,
// SimpleDateFormat("dd.MM.yyyy") de onu "01.09.2026" metnine

fun formatTl(deger: Double): String = String.format(TR, "%.2f ₺", deger)

fun formatLitre(deger: Double): String = String.format(TR, "%.2f L", deger)

// %,d binlik ayraci koyar: 1234 -> "1.234 km"
fun formatKm(deger: Int): String = String.format(TR, "%,d km", deger)

fun formatSaat(saat: Int, dakika: Int): String =
    String.format(TR, "%02d:%02d", saat, dakika)

// DatePicker sadece gunu, TimePicker sadece saat/dakikayi veriyor.
// Modeldeki tek Long ikisini birden tasidigi icin burada birlestiriliyor.
fun tarihSaatBirlestir(gunMillis: Long, saat: Int, dakika: Int): Long {
    val takvim = Calendar.getInstance()
    takvim.timeInMillis = gunMillis
    takvim.set(Calendar.HOUR_OF_DAY, saat)
    takvim.set(Calendar.MINUTE, dakika)
    takvim.set(Calendar.SECOND, 0)
    takvim.set(Calendar.MILLISECOND, 0)
    return takvim.timeInMillis
}

// Kayitli bir tarihi forma yuklerken saat ve dakikayi geri ayirmak icin.
fun saatAl(millis: Long): Int {
    val takvim = Calendar.getInstance()
    takvim.timeInMillis = millis
    return takvim.get(Calendar.HOUR_OF_DAY)
}

fun dakikaAl(millis: Long): Int {
    val takvim = Calendar.getInstance()
    takvim.timeInMillis = millis
    return takvim.get(Calendar.MINUTE)
}
