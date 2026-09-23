package com.oguzhanp.motorum.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry
import com.oguzhanp.motorum.core.tasarim.AppMotion

// Ekranlar arasi gecisler (tasarim: Hareket). Hareketin yonu kullaniciya
// nereye gittigini soyluyor:
//  - ALTTAN: bir sey olusturuyorsun (Kayit Ekle, Motor Ekle, Belge). Ekran
//    alttan yukselip ustune biniyor.
//  - SAGDAN: bir seyin icine giriyorsun (detaylar, Belgeler, Istatistik).
//    Sagdan geliyor, alttaki hafifce sola kayip soluklasiyor.
//  - YERINDE: esit yerler arasinda geciyorsun (alt bar sekmeleri, giris ve
//    uye ol). Yon yok; icerik yerinde soluklasip degisiyor. Maketteki hafif
//    buyume yok: alt bar her sekmenin icinde cizildigi icin o da buyurdu.
// Geri donus hepsinde ayni: kapanan ekran saga kayip cikiyor. Alttan acilan
// ekranin asagi inmesi ve sekmelerin yerinde sonmesi geri tusunda garip
// duruyordu; tek bir yon (saga) geri hareketini her yerde ayni hissettiriyor.
//
// "Animasyonlari kaldir" acik bir telefonda bunlar kendiliginden aninda
// oluyor: Compose sistemin animasyon olcegine uyuyor, ayrica kod gerekmiyor.
enum class GecisTuru { ALTTAN, SAGDAN, YERINDE }

// Rotalarin parametreleri (? ve / sonrasi) atilip tabanlari karsilastiriliyor.
// Listede olmayan her ekran YERINDE. Yeni bir ekran eklerken turunu buradan sec.
private val ALTTAN = tabanlar(Routes.KAYIT_EKLE_KALIBI, Routes.MOTOR_EKLE, Routes.BELGE)
private val SAGDAN = tabanlar(Routes.KAYIT_DETAY, Routes.MOTOR_DETAY, Routes.BELGELER, Routes.ISTATISTIK)

private fun taban(rota: String) = rota.substringBefore('?').substringBefore('/')
private fun tabanlar(vararg rotalar: String) = rotalar.map(::taban).toSet()

private fun gecisTuru(rota: String?): GecisTuru {
    val taban = rota?.let(::taban) ?: return GecisTuru.YERINDE
    return when (taban) {
        in ALTTAN -> GecisTuru.ALTTAN
        in SAGDAN -> GecisTuru.SAGDAN
        else -> GecisTuru.YERINDE
    }
}

private val NavBackStackEntry.tur: GecisTuru get() = gecisTuru(destination.route)

private fun <T> giris() = tween<T>(AppMotion.EKRAN_GIRIS, easing = AppMotion.egri)
private fun <T> cikis() = tween<T>(AppMotion.EKRAN_CIKIS, easing = AppMotion.cikisEgrisi)

// Alttaki ekranin ne kadar sola kacip ne kadar soluklastigi.
private const val GERI_KAYMA = 5 // genisligin 1/5'i
private const val SOLUK = 0.6f

// ----- NavHost'a verilen dort kural. Hangi hareketin oynayacagini gelen ya da
// giden ekranin turu belirliyor.

// Ileri giderken: gelen ekran.
fun AnimatedContentTransitionScope<NavBackStackEntry>.ekranGirisi(): EnterTransition =
    when (targetState.tur) {
        GecisTuru.ALTTAN -> slideInVertically(giris()) { it } + fadeIn(giris())
        GecisTuru.SAGDAN -> slideInHorizontally(giris()) { it }
        GecisTuru.YERINDE -> fadeIn(giris())
    }

// Ileri giderken: alttaki ekran. Yeni ekranin turune bakiyor.
fun AnimatedContentTransitionScope<NavBackStackEntry>.ekranCikisi(): ExitTransition =
    when (targetState.tur) {
        // Ustune katman geliyor; yerinde kalip hafifce soluklasiyor.
        GecisTuru.ALTTAN -> fadeOut(giris(), targetAlpha = SOLUK)
        GecisTuru.SAGDAN -> slideOutHorizontally(giris()) { -it / GERI_KAYMA } +
                fadeOut(giris(), targetAlpha = SOLUK)
        // Gelen ekran ustte soluklasarak beliriyor; alttaki sonuna kadar tam
        // gorunur kaliyor. Ikisi birden soluklassa, iki ekranda da ayni olan
        // alt bar gecis boyunca bir an siliklesirdi.
        GecisTuru.YERINDE -> fadeOut(tween(durationMillis = 1, delayMillis = AppMotion.EKRAN_GIRIS))
    }

// Geri donerken: alttan tekrar gorunen ekran. Kapanan ekranin turune bakiyor.
fun AnimatedContentTransitionScope<NavBackStackEntry>.geriGirisi(): EnterTransition =
    when (initialState.tur) {
        GecisTuru.ALTTAN -> fadeIn(giris(), initialAlpha = SOLUK)
        GecisTuru.SAGDAN -> slideInHorizontally(giris()) { -it / GERI_KAYMA } +
                fadeIn(giris(), initialAlpha = SOLUK)
        // Geri donuste kapanan ekran ustte; alttaki oldugu gibi bekliyor.
        GecisTuru.YERINDE -> EnterTransition.None
    }

// Geri donerken: kapanan ekran, turu ne olursa olsun saga cikiyor.
// Parmakla geri kaydirmada (tahminli geri) da bu kullaniliyor (MotorumApp'te
// predictivePop... olarak ayrica veriliyor): sayfa parmagi takip ederek saga kayiyor.
@Suppress("UnusedReceiverParameter")
fun AnimatedContentTransitionScope<NavBackStackEntry>.geriCikisi(): ExitTransition =
    slideOutHorizontally(cikis()) { it }
