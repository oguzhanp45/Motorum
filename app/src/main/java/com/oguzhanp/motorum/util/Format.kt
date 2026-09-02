package com.oguzhanp.motorum.util
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTarih(millis: Long): String =
    SimpleDateFormat("dd.MM.yyyy", Locale("tr")).format(Date(millis))

//formatTl: %.2f = "ondalıklı sayıyı 2 basamakla yaz".
//%.0f yaparsan kuruş görünmez
//tarihMillis). Date(millis) bu sayıyı tarih nesnesine çevirir,
// SimpleDateFormat("dd.MM.yyyy") de onu "01.09.2026" metnine

fun formatTl(deger: Double): String = String.format(Locale("tr"), "%.2f ₺", deger)

fun formatLitre(deger: Double): String = String.format(Locale("tr"), "%.2f L", deger)