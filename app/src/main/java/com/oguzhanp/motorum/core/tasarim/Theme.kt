package com.oguzhanp.motorum.core.tasarim

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp

// Iki tema, tek kurulum. dynamicColor bilerek kapali: tasarim kendi renkleriyle
// hazirlandi, duvar kagidindan renk uretmek onu her telefonda bozardi.
//
// karanlik su an telefonun ayarindan geliyor. Ayarlar ekranina "Acik /
// Karanlik / Sistem" secimi geldiginde deger oradan verilecek; bu fonksiyonun
// degismesi gerekmiyor.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotorumTheme(
    karanlik: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val renkler = if (karanlik) KaranlikRenkler else AcikRenkler

    // Iki kanaldan birden veriyoruz: Material bilesenleri (Button, Card,
    // TextField...) colorScheme'i okuyor, bizim ekranlarimiz MotorumRenkleri'ni.
    // Ikisi ayni takimdan kuruldugu icin birbiriyle celismiyor.
    //
    // Dokunma dalgasi her yerde murekkep, %12 (tasarim: Dokunma). Kartin kendi
    // vurgu rengi degil: butun uygulamada tek dil.
    val dalga = RippleConfiguration(
        color = renkler.murekkep,
        rippleAlpha = RippleAlpha(
            draggedAlpha = 0.16f,
            focusedAlpha = 0.12f,
            hoveredAlpha = 0.08f,
            pressedAlpha = 0.12f
        )
    )
    CompositionLocalProvider(
        LocalMotorumRenkleri provides renkler,
        LocalRippleConfiguration provides dalga
    ) {
        MaterialTheme(
            colorScheme = renkSemasi(renkler),
            typography = Typography,
            shapes = SEKILLER,
            content = content
        )
    }
}

// Metin alanlari, acilir menuler ve snackbar Material'in "extraSmall" kosesini
// kullaniyor (varsayilani 4 dp). Tasarimdaki alanlar 12 dp: tek yerden
// degistirince butun formlar ayni koseye oturuyor.
private val SEKILLER = Shapes(extraSmall = RoundedCornerShape(12.dp))

// Material'in renk rollerini bizim takimdan dolduruyoruz. Material'in kendi
// varsayilanlarina hic birakmadigimiz roller onemli: yeni Material bilesenleri
// (alt bar, diyalog, alt panel, tarih secici) yuzeylerini "surfaceContainer"
// ailesinden aliyor ve varsayilani mor tonlu. Bunlari doldurmasaydik karanlik
// temada diyaloglar morumsu, acik temada alt bar lila kalirdi.
private fun renkSemasi(r: MotorumRenkleri): ColorScheme {
    val temel = if (r.karanlik) darkColorScheme() else lightColorScheme()
    return temel.copy(
        // Birincil renk murekkep: dugmeler, FAB, odaklanan alan cercevesi ve
        // secim isaretleri. Mavi sadece baglanti ve bilgi rengi olarak kaliyor,
        // yani "tiklanabilir metin" ile "ana eylem" birbirine karismiyor.
        primary = r.murekkep,
        onPrimary = r.murekkepUstu,
        primaryContainer = r.cizgiSolgun,
        onPrimaryContainer = r.metinAna,
        secondary = r.metinIkincil,
        onSecondary = r.kartZemin,
        secondaryContainer = r.cizgiSolgun,
        onSecondaryContainer = r.metinAna,
        tertiary = r.aksiyonMavi,
        onTertiary = r.kartZemin,

        background = r.zemin,
        onBackground = r.metinAna,
        surface = r.kartZemin,
        onSurface = r.metinAna,
        surfaceVariant = r.kartZemin,
        onSurfaceVariant = r.metinIkincil,
        // Yukseklik renk tonuyla degil golgeyle anlatiliyor: tonu yuzeyin
        // kendisi yapinca Material yukselen yuzeyleri boyamiyor.
        surfaceTint = r.kartZemin,

        surfaceContainerLowest = r.kartZemin,
        surfaceContainerLow = r.kartZemin,
        surfaceContainer = r.kartZemin,
        surfaceContainerHigh = r.kartZemin,
        surfaceContainerHighest = r.cizgiSolgun,
        surfaceBright = r.kartZemin,
        surfaceDim = r.zemin,

        outline = r.kenar,
        outlineVariant = r.cizgiSolgun,

        error = r.hataKirmizi,
        onError = r.kartZemin,

        // Snackbar ters renkte ciziliyor: acik temada koyu, karanlikta acik
        // serit. "Geri Al" dugmesi de ona gore mavinin okunan tonunu aliyor.
        inverseSurface = r.murekkep,
        inverseOnSurface = r.murekkepUstu,
        inversePrimary = if (r.karanlik) AcikRenkler.baglantiMavi else KaranlikRenkler.baglantiMavi
    )
}
