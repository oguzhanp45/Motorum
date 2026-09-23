package com.oguzhanp.motorum.core.tasarim

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import com.oguzhanp.motorum.core.tasarim.AppMotion
import com.oguzhanp.motorum.core.tasarim.Murekkep
import com.oguzhanp.motorum.core.tasarim.MurekkepBasili
import com.oguzhanp.motorum.core.tasarim.MurekkepUstu

// Dokunma geri bildirimi (tasarim: Dokunma). Basilan sey hafifce kuculuyor:
// basma hizli (110 ms), birakma yavas (220 ms). Kartlar ve satirlar biraz,
// dugmeler biraz daha fazla; dugmeler ayrica bir ton koyulasiyor.
const val KART_BASILI = 0.968f
const val DUGME_BASILI = 0.958f

// Kullanim: ayni etkilesim kaynagi hem buraya hem tiklanan bilesene
// (Card, Button, clickable) veriliyor; basili olup olmadigini oradan okuyoruz.
// Zincirin BASINA konmali: boylece golge ve zemin de birlikte kuculuyor.
@Composable
fun Modifier.basilincaKucul(
    etkilesim: InteractionSource,
    olcek: Float = KART_BASILI
): Modifier {
    val basili by etkilesim.collectIsPressedAsState()
    val deger by animateFloatAsState(
        targetValue = if (basili) olcek else 1f,
        animationSpec = tween(
            if (basili) AppMotion.BASMA else AppMotion.BIRAKMA,
            easing = AppMotion.egri
        ),
        label = "basilma"
    )
    // Deger sadece cizim katmaninda okunuyor: basarken yerlesim yeniden hesaplanmiyor.
    return this.graphicsLayer {
        scaleX = deger
        scaleY = deger
    }
}

// Murekkep zeminli dugmelerin rengi: basiliyken bir ton koyu.
@Composable
fun basiliMurekkep(etkilesim: InteractionSource): Color {
    val basili by etkilesim.collectIsPressedAsState()
    val renk by animateColorAsState(
        targetValue = if (basili) MurekkepBasili else Murekkep,
        animationSpec = tween(if (basili) AppMotion.BASMA else AppMotion.BIRAKMA),
        label = "basiliMurekkep"
    )
    return renk
}

// Uygulamanin ana dugmesi: Material'in Button'u, ustune basilma hissi.
// Kaydet, Guncelle, Motor Ekle... hepsi bundan; ayni dokunusu her yerde ayni
// hissettirmek icin tek yerde.
@Composable
fun MurekkepDugme(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val etkilesim = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        elevation = elevation,
        contentPadding = contentPadding,
        interactionSource = etkilesim,
        colors = ButtonDefaults.buttonColors(
            containerColor = basiliMurekkep(etkilesim),
            contentColor = MurekkepUstu
        ),
        modifier = Modifier
            .basilincaKucul(etkilesim, DUGME_BASILI)
            .then(modifier),
        content = content
    )
}
