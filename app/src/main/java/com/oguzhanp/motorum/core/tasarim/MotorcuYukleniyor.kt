package com.oguzhanp.motorum.core.tasarim

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.core.tasarim.AppSpacing
import com.oguzhanp.motorum.core.tasarim.MetinIkincil

// Sayfa boyu bekleme icin. Dönen daire yerine motorcu: bekleme suresi ayni
// ama uygulamanin kendi sesiyle konusuyor.
//
// Buton icindeki kucuk beklemelerde KULLANILMIYOR; orada dönen daire dogru
// olan, butona 140 dp'lik bir animasyon sigmaz.
@Composable
fun MotorcuYukleniyor(
    modifier: Modifier = Modifier,
    mesaj: String? = null,
    boyut: Dp = 140.dp
) {
    val kompozisyon by rememberLottieComposition(LottieCompositionSpec.RawRes(motorcuDosyasi()))

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.kucuk)
    ) {
        LottieAnimation(
            composition = kompozisyon,
            // Dosya kusursuz donguye gore hazirlandi: basi ve sonu ayni kare,
            // sonsuz tekrarda duraklama gorunmuyor.
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(boyut)
        )

        if (mesaj != null) {
            Text(
                text = mesaj,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MetinIkincil
            )
        }
    }
}

// Asagi cekme gostergesi. Ayni motorcu, kucuk hali: cekerken animasyon
// parmagin hareketiyle ilerliyor, birakip yenileme baslayinca kendi
// hizinda donmeye geciyor. Dönen daire yerine bu, cunku cekme hareketinin
// kendisi zaten "ilerleme" anlatiyor.
@Composable
fun MotorcuCekmeGostergesi(
    ilerleme: Float,
    yenileniyor: Boolean,
    modifier: Modifier = Modifier
) {
    val kompozisyon by rememberLottieComposition(LottieCompositionSpec.RawRes(motorcuDosyasi()))

    // Yenileme sirasinda kendi kendine donen ilerleme. isPlaying false iken
    // duruyor, o zaman asagidaki lambda cekme oranini kullaniyor.
    val donguIlerlemesi by animateLottieCompositionAsState(
        composition = kompozisyon,
        iterations = LottieConstants.IterateForever,
        isPlaying = yenileniyor
    )

    val oran = ilerleme.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .graphicsLayer {
                // Gosterge parmakla birlikte asagi iniyor. Yenilerken sabit
                // duruyor ki liste altinda titremesin.
                translationY = if (yenileniyor) INIS.toPx() else INIS.toPx() * oran
                alpha = if (yenileniyor) 1f else oran
            }
            .size(CEKME_BOYUTU)
    ) {
        LottieAnimation(
            composition = kompozisyon,
            // Iki kaynak tek yerde birlesiyor: yenilerken dongu, cekerken parmak.
            progress = { if (yenileniyor) donguIlerlemesi else oran },
            modifier = Modifier.size(CEKME_BOYUTU)
        )
    }
}

private val CEKME_BOYUTU = 52.dp
private val INIS = 16.dp

// Temaya gore hangi motorcu dosyasi. Uc yer kullaniyor (sayfa yukleyicisi,
// asagi cekme, bos durum), secim tek yerde duruyor.
//
// Animasyonun grileri acik zemine gore cizilmis; koyu kartin uzerinde
// motorcu kayboluyor. Karanlik icin grileri terslenmis ikinci dosya var.
// isSystemInDarkTheme() yerine temanin kendi yuzey rengine bakiyoruz:
// uygulama su an tek temali, sistem karanlikta olsa bile ekran acik.
// Karanlik tema baglandiginda burasi kendiliginden dogru dosyayi secer.
@Composable
internal fun motorcuDosyasi(): Int =
    if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) R.raw.motorcu_karanlik
    else R.raw.motorcu
