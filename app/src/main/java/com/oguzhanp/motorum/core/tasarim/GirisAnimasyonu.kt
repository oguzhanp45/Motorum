package com.oguzhanp.motorum.core.tasarim

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.oguzhanp.motorum.core.tasarim.AppMotion
import kotlinx.coroutines.delay

// Form alanlarinin sirayla yukselerek belirmesi (tasarim: Kayit Ekle, "yuksel").
// Her parca bir oncekinden 50 ms sonra basliyor; hareket 10 dp asagidan, tek
// egriyle. Sadece ilk acilista oynuyor: kategori degisince tekrar etmiyor.
@Composable
fun Modifier.yukselerekGir(sira: Int): Modifier {
    // Onizlemede animasyon oynamiyor: alanlar gorunmez kalmasin.
    if (LocalInspectionMode.current) return this
    val ilerleme = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(sira * GECIKME_MS)
        ilerleme.animateTo(1f, tween(SURE_MS, easing = AppMotion.egri))
    }
    // Deger sadece cizim katmaninda okunuyor: alan her karede yeniden kurulmuyor.
    return this.graphicsLayer {
        alpha = ilerleme.value
        translationY = (1f - ilerleme.value) * 10.dp.toPx()
    }
}

private const val GECIKME_MS = 50L
private const val SURE_MS = 520
