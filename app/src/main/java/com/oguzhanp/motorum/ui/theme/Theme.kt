package com.oguzhanp.motorum.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// dynamicColor ve karanlik tema kaldirildi: tasarim tek bir acik temaya gore
// hazirlandi, duvar kagidindan renk uretmek onu her telefonda bozardi.
// Karanlik tema geldiginde burasi ikiye ayrilacak.
private val LightColorScheme = lightColorScheme(
    primary = AksiyonMavi,
    onPrimary = KartZemin,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Zemin,
    surface = KartZemin,
    surfaceVariant = KartZemin,
    onBackground = MetinAna,
    onSurface = MetinAna,
    onSurfaceVariant = MetinIkincil,
    outlineVariant = CizgiSolgun,
    // M3'un varsayilan hata kirmizisi bizim paletimize ait degil. Form hatalari da
    // cikis butonu da colorScheme.error kullandigi icin tek yerden veriyoruz.
    error = HataKirmizi,
    onError = KartZemin
)

@Composable
fun MotorumTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
