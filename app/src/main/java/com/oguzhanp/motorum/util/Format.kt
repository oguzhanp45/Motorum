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

fun formatBirimFiyat(deger: Double): String = String.format(TR, "%.2f ₺/L", deger)

fun formatKmMaliyet(deger: Double): String = String.format(TR, "%.2f ₺/km", deger)

// Duzenleme alanina yazilacak sayi. Digerlerinden farki birim ve binlik ayraci
// koymamasi: kullanici bu metni duzenleyip geri gonderecek, sonra tekrar sayiya
// cevrilecek. Tam sayida ondalik hic yazilmiyor (1200.0 -> "1200"), ondalikli
// sayida virgul kullaniliyor; form zaten virgulu noktaya cevirerek okuyor.
fun sayiyiYaziya(deger: Double): String =
    if (deger % 1.0 == 0.0) deger.toLong().toString()
    else deger.toString().replace('.', ',')

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

// Hazir araliklar icin: "3 ay sonra". Calendar ay sonunu kendisi duzeltiyor;
// 31 Ocak + 1 ay = 28 (ya da 29) Subat, 3 Mart'a tasmiyor.
fun ayEkle(millis: Long, ay: Int): Long {
    val takvim = Calendar.getInstance()
    takvim.timeInMillis = millis
    takvim.add(Calendar.MONTH, ay)
    return takvim.timeInMillis
}

// Saat onemsiz, sadece takvim gunu karsilastiriliyor.
fun ayniGun(a: Long, b: Long): Boolean = formatTarih(a) == formatTarih(b)

// "12 Haziran". Bu yil degilse yil da ekleniyor: "12 Haziran 2027".
fun formatGunAy(millis: Long): String {
    val buYil = Calendar.getInstance().get(Calendar.YEAR)
    val yil = Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.YEAR)
    val kalip = if (yil == buYil) "d MMMM" else "d MMMM yyyy"
    return SimpleDateFormat(kalip, TR).format(Date(millis))
}
