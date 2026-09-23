package com.oguzhanp.motorum.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Uygulamanin renk takimi, iki temada.
//
// Eskiden her renk sabit bir degerdi (val MetinIkincil = Color(...)). Tek tema
// varken bu yetiyordu; karanlik tema gelince ayni isim iki farkli deger
// tasimali. Cozum: degerler bu siniftan geliyor, tema hangisini verdiyse o.
//
// Isimler DEGISMEDI. Ekranlarda "color = MetinIkincil" yazan yuzden fazla
// satir aynen kaliyor; sadece arkadaki deger artik temaya bakiyor.
@Immutable
data class MotorumRenkleri(
    val karanlik: Boolean,

    // Yuzeyler
    val zemin: Color,
    val kartZemin: Color,
    val cizgiSolgun: Color,
    val sekmeZemin: Color,
    val kenar: Color,
    val murekkep: Color,
    val murekkepUstu: Color,
    // Basili murekkep dugme: bir ton koyu (tasarim: Dokunma).
    val murekkepBasili: Color,

    // Metin
    val metinAna: Color,
    val metinEtiket: Color,
    val metinIkincil: Color,
    val metinSolgun: Color,

    // Kategoriler: renk (ikon, cizgi), zemin (rozet arkasi), metin (rozet yazisi)
    val yakitRenk: Color, val yakitZemin: Color, val yakitMetin: Color,
    val roadTripRenk: Color, val roadTripZemin: Color, val roadTripMetin: Color,
    val bakimRenk: Color, val bakimZemin: Color, val bakimMetin: Color,
    val aksesuarRenk: Color, val aksesuarZemin: Color, val aksesuarMetin: Color,

    // Durum ve eylem
    val aksiyonMavi: Color,
    val aksiyonMaviZemin: Color,
    val baglantiMavi: Color,
    val hataKirmizi: Color,
    val hataZemin: Color,
    val hataMetin: Color,
    val hataSolgun: Color,
    val durumYesilZemin: Color,
    val durumYesilMetin: Color,
    val uyariMetin: Color,

    // Motor avatarlari. Kategori paletinden bilerek uzak: kategoriler gul,
    // turkuaz, kehribar ve mor; motorlar mavi-gri ailesinde kaliyor ki bir kart
    // ikonuna bakinca "bu bakim mi, motor mu" karismasin.
    val motorIndigo: Color, val motorIndigoZemin: Color,
    val motorGok: Color, val motorGokZemin: Color,
    val motorLacivert: Color, val motorLacivertZemin: Color,
    val motorArduvaz: Color, val motorArduvazZemin: Color,
    val motorCamgobegi: Color, val motorCamgobegiZemin: Color
)

internal val AcikRenkler = MotorumRenkleri(
    karanlik = false,

    zemin = Color(0xFFF8FAFC),
    kartZemin = Color(0xFFFFFFFF),
    cizgiSolgun = Color(0xFFF1F5F9),
    sekmeZemin = Color(0xFFE2E8F0),
    kenar = Color(0xFFCBD5E1),
    murekkep = Color(0xFF111827),
    murekkepUstu = Color(0xFFFFFFFF),
    murekkepBasili = Color(0xFF0B1220),

    metinAna = Color(0xFF0F172A),
    metinEtiket = Color(0xFF334155),
    metinIkincil = Color(0xFF64748B),
    metinSolgun = Color(0xFF94A3B8),

    yakitRenk = Color(0xFFF43F5E), yakitZemin = Color(0xFFFFF1F2), yakitMetin = Color(0xFFBE123C),
    roadTripRenk = Color(0xFF14B8A6), roadTripZemin = Color(0xFFF0FDFA), roadTripMetin = Color(0xFF0F766E),
    bakimRenk = Color(0xFFF59E0B), bakimZemin = Color(0xFFFFFBEB), bakimMetin = Color(0xFFB45309),
    aksesuarRenk = Color(0xFFA855F7), aksesuarZemin = Color(0xFFFAF5FF), aksesuarMetin = Color(0xFF7E22CE),

    aksiyonMavi = Color(0xFF3B82F6),
    aksiyonMaviZemin = Color(0xFFEFF6FF),
    baglantiMavi = Color(0xFF2563EB),
    hataKirmizi = Color(0xFFEF4444),
    hataZemin = Color(0xFFFEF2F2),
    hataMetin = Color(0xFFB91C1C),
    hataSolgun = Color(0xFFFCA5A5),
    durumYesilZemin = Color(0xFFECFDF5),
    durumYesilMetin = Color(0xFF047857),
    uyariMetin = Color(0xFFB45309),

    motorIndigo = Color(0xFF4F46E5), motorIndigoZemin = Color(0xFFEEF2FF),
    motorGok = Color(0xFF0EA5E9), motorGokZemin = Color(0xFFF0F9FF),
    motorLacivert = Color(0xFF1D4ED8), motorLacivertZemin = Color(0xFFEFF6FF),
    motorArduvaz = Color(0xFF475569), motorArduvazZemin = Color(0xFFF1F5F9),
    motorCamgobegi = Color(0xFF0891B2), motorCamgobegiZemin = Color(0xFFECFEFF)
)

// Karanlik degerler karanlik maketlerden alindi: her acik renk maketlerde
// hangi karanlik renge donustuyse o. Mantigi uc cumle:
//  - Zemin cok koyu, kartlar bir tik acik; yukseklik golgeyle degil acilikla okunuyor.
//  - Murekkep tersine donuyor: acik temada koyu dugme, karanlikta acik dugme.
//  - Kategori renkleri bir kademe parlaklasiyor, zeminleri renk tonlu koyuya iniyor.
//
// Motor avatarlari ve iki mavi tonu maketlerde karanlik halde gecmiyordu;
// onlari ayni kurala gore ben sectim.
internal val KaranlikRenkler = MotorumRenkleri(
    karanlik = true,

    zemin = Color(0xFF07090E),
    kartZemin = Color(0xFF141B29),
    cizgiSolgun = Color(0xFF1E2635),
    sekmeZemin = Color(0xFF252F41),
    kenar = Color(0xFF47536A),
    murekkep = Color(0xFFF1F5F9),
    murekkepUstu = Color(0xFF0B1220),
    murekkepBasili = Color(0xFFE2E8F0),

    metinAna = Color(0xFFF1F5F9),
    metinEtiket = Color(0xFFCBD5E1),
    metinIkincil = Color(0xFF94A3B8),
    metinSolgun = Color(0xFF6B7683),

    yakitRenk = Color(0xFFF43F5E), yakitZemin = Color(0xFF2B1620), yakitMetin = Color(0xFFFDA4AF),
    roadTripRenk = Color(0xFF2DD4BF), roadTripZemin = Color(0xFF0E2A27), roadTripMetin = Color(0xFF5EEAD4),
    bakimRenk = Color(0xFFFBBF24), bakimZemin = Color(0xFF2B2213), bakimMetin = Color(0xFFFBBF24),
    aksesuarRenk = Color(0xFFC084FC), aksesuarZemin = Color(0xFF251A33), aksesuarMetin = Color(0xFFD8B4FE),

    aksiyonMavi = Color(0xFF60A5FA),
    aksiyonMaviZemin = Color(0xFF172554),
    baglantiMavi = Color(0xFF93C5FD),
    hataKirmizi = Color(0xFFF87171),
    hataZemin = Color(0xFF2B1618),
    hataMetin = Color(0xFFFCA5A5),
    hataSolgun = Color(0xFFF87171),
    durumYesilZemin = Color(0xFF0E2A20),
    durumYesilMetin = Color(0xFF6EE7B7),
    uyariMetin = Color(0xFFFBBF24),

    motorIndigo = Color(0xFF818CF8), motorIndigoZemin = Color(0xFF1E1B4B),
    motorGok = Color(0xFF38BDF8), motorGokZemin = Color(0xFF0C2A3A),
    motorLacivert = Color(0xFF93C5FD), motorLacivertZemin = Color(0xFF172554),
    motorArduvaz = Color(0xFFCBD5E1), motorArduvazZemin = Color(0xFF1E2635),
    motorCamgobegi = Color(0xFF22D3EE), motorCamgobegiZemin = Color(0xFF083344)
)

// Tema bu kutuya hangi takimi koyarsa ekranlar onu okuyor. "static" cunku
// tema nadiren degisiyor; degistiginde de butun agacin yeniden cizilmesi
// dogru olan zaten.
internal val LocalMotorumRenkleri = staticCompositionLocalOf { AcikRenkler }

// ------------------------------------------------------------ KISAYOLLAR
//
// Eski sabitlerin yerini alan okuyucular. Her biri o an gecerli temanin
// degerini veriyor. @Composable cunku temayi okumak icin agacin icinde olmak
// gerekiyor: bir cizim blogunun (Canvas) ya da duz bir fonksiyonun icinden
// okunamazlar, oralara degeri disarida alip vermek gerekiyor.

private val r: MotorumRenkleri
    @Composable @ReadOnlyComposable get() = LocalMotorumRenkleri.current

// Ekranin karanlik temada cizilip cizilmedigi. Kendi renklerini tasiyan
// bilesenler (hava seridi gibi) hangi takimi kullanacagini buradan seciyor.
val karanlikTema: Boolean @Composable @ReadOnlyComposable get() = r.karanlik

val Zemin: Color @Composable @ReadOnlyComposable get() = r.zemin
val KartZemin: Color @Composable @ReadOnlyComposable get() = r.kartZemin
val CizgiSolgun: Color @Composable @ReadOnlyComposable get() = r.cizgiSolgun
val SekmeZemin: Color @Composable @ReadOnlyComposable get() = r.sekmeZemin
val Kenar: Color @Composable @ReadOnlyComposable get() = r.kenar
val Murekkep: Color @Composable @ReadOnlyComposable get() = r.murekkep
val MurekkepUstu: Color @Composable @ReadOnlyComposable get() = r.murekkepUstu
val MurekkepBasili: Color @Composable @ReadOnlyComposable get() = r.murekkepBasili

val MetinAna: Color @Composable @ReadOnlyComposable get() = r.metinAna
val MetinEtiket: Color @Composable @ReadOnlyComposable get() = r.metinEtiket
val MetinIkincil: Color @Composable @ReadOnlyComposable get() = r.metinIkincil
val MetinSolgun: Color @Composable @ReadOnlyComposable get() = r.metinSolgun

val YakitRenk: Color @Composable @ReadOnlyComposable get() = r.yakitRenk
val YakitZemin: Color @Composable @ReadOnlyComposable get() = r.yakitZemin
val YakitMetin: Color @Composable @ReadOnlyComposable get() = r.yakitMetin
val RoadTripRenk: Color @Composable @ReadOnlyComposable get() = r.roadTripRenk
val RoadTripZemin: Color @Composable @ReadOnlyComposable get() = r.roadTripZemin
val RoadTripMetin: Color @Composable @ReadOnlyComposable get() = r.roadTripMetin
val BakimRenk: Color @Composable @ReadOnlyComposable get() = r.bakimRenk
val BakimZemin: Color @Composable @ReadOnlyComposable get() = r.bakimZemin
val BakimMetin: Color @Composable @ReadOnlyComposable get() = r.bakimMetin
val AksesuarRenk: Color @Composable @ReadOnlyComposable get() = r.aksesuarRenk
val AksesuarZemin: Color @Composable @ReadOnlyComposable get() = r.aksesuarZemin
val AksesuarMetin: Color @Composable @ReadOnlyComposable get() = r.aksesuarMetin

// Kategori renkleri kimligi, bu ikisi durumu anlatiyor:
// mavi = tiklanabilir, yesil = tamamlandi.
val AksiyonMavi: Color @Composable @ReadOnlyComposable get() = r.aksiyonMavi
val AksiyonMaviZemin: Color @Composable @ReadOnlyComposable get() = r.aksiyonMaviZemin
val BaglantiMavi: Color @Composable @ReadOnlyComposable get() = r.baglantiMavi
// Yikici islemler: cikis, silme.
val HataKirmizi: Color @Composable @ReadOnlyComposable get() = r.hataKirmizi
// Kirmizi rozet ("Verilmedi") ve cikis satirinin ikon zemini / oku.
val HataZemin: Color @Composable @ReadOnlyComposable get() = r.hataZemin
val HataMetin: Color @Composable @ReadOnlyComposable get() = r.hataMetin
val HataSolgun: Color @Composable @ReadOnlyComposable get() = r.hataSolgun
val DurumYesilZemin: Color @Composable @ReadOnlyComposable get() = r.durumYesilZemin
val DurumYesilMetin: Color @Composable @ReadOnlyComposable get() = r.durumYesilMetin
// Formda engellemeyen uyarilar icin (ornegin sayacin geriye gitmesi).
val UyariMetin: Color @Composable @ReadOnlyComposable get() = r.uyariMetin

val MotorIndigo: Color @Composable @ReadOnlyComposable get() = r.motorIndigo
val MotorIndigoZemin: Color @Composable @ReadOnlyComposable get() = r.motorIndigoZemin
val MotorGok: Color @Composable @ReadOnlyComposable get() = r.motorGok
val MotorGokZemin: Color @Composable @ReadOnlyComposable get() = r.motorGokZemin
val MotorLacivert: Color @Composable @ReadOnlyComposable get() = r.motorLacivert
val MotorLacivertZemin: Color @Composable @ReadOnlyComposable get() = r.motorLacivertZemin
val MotorArduvaz: Color @Composable @ReadOnlyComposable get() = r.motorArduvaz
val MotorArduvazZemin: Color @Composable @ReadOnlyComposable get() = r.motorArduvazZemin
val MotorCamgobegi: Color @Composable @ReadOnlyComposable get() = r.motorCamgobegi
val MotorCamgobegiZemin: Color @Composable @ReadOnlyComposable get() = r.motorCamgobegiZemin
