package com.oguzhanp.motorum.core.tasarim

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.oguzhanp.motorum.R

// Inter degisken (variable) font: tum kalinliklar tek dosyada.
// Ayni dosyayi dort kez, her seferinde farkli agirlik ekseniyle tanitiyoruz.
// Bunu yapmasaydik Compose kalin yaziyi yapay olarak uretir, cirkin gorunurdu.
private fun interFont(agirlik: Int, fontWeight: FontWeight) = Font(
    resId = R.font.inter,
    weight = fontWeight,
    variationSettings = FontVariation.Settings(FontVariation.weight(agirlik))
)

val Inter = FontFamily(
    interFont(400, FontWeight.Normal),
    interFont(500, FontWeight.Medium),
    interFont(600, FontWeight.SemiBold),
    interFont(700, FontWeight.Bold)
)

// Font ailesi on bes rolun hepsine veriliyor: Button, AlertDialog, TextField gibi
// bilesenler biz yazmasak da kendi rollerini cekiyor, biri atlanirsa orasi Roboto kalir.
// Boyut ve kalinlik ise sadece tasarimin belirledigi yedi rolde degisiyor.
private val V = Typography()

val Typography = Typography(
    displayLarge = V.displayLarge.copy(fontFamily = Inter),
    displayMedium = V.displayMedium.copy(fontFamily = Inter),
    displaySmall = V.displaySmall.copy(fontFamily = Inter),

    headlineLarge = V.headlineLarge.copy(fontFamily = Inter),
    headlineMedium = V.headlineMedium.copy(fontFamily = Inter),
    headlineSmall = V.headlineSmall.copy(fontFamily = Inter),

    titleLarge = V.titleLarge.copy(
        fontFamily = Inter,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.Bold
    ),
    titleMedium = V.titleMedium.copy(
        fontFamily = Inter,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Bold
    ),
    titleSmall = V.titleSmall.copy(
        fontFamily = Inter,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Bold
    ),

    bodyLarge = V.bodyLarge.copy(fontFamily = Inter),
    bodyMedium = V.bodyMedium.copy(
        fontFamily = Inter,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    bodySmall = V.bodySmall.copy(
        fontFamily = Inter,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),

    labelLarge = V.labelLarge.copy(fontFamily = Inter),
    labelMedium = V.labelMedium.copy(
        fontFamily = Inter,
        fontSize = 12.sp,
        lineHeight = 15.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),
    labelSmall = V.labelSmall.copy(
        fontFamily = Inter,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp
    )
)
