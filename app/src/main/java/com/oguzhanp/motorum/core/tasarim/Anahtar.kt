package com.oguzhanp.motorum.core.tasarim

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.oguzhanp.motorum.core.tasarim.AppMotion
import com.oguzhanp.motorum.core.tasarim.AppShape
import com.oguzhanp.motorum.core.tasarim.KartZemin
import com.oguzhanp.motorum.core.tasarim.MotorumTheme
import com.oguzhanp.motorum.core.tasarim.Murekkep
import com.oguzhanp.motorum.core.tasarim.SekmeZemin

// Tasarimdaki acma/kapama anahtari: 46x27 serit, 21'lik topuz. Material'in
// Switch'i daha buyuk ve kapaliyken topuzu kuculuyor; maketteki sade
// gorunumu vermiyordu. Acik = murekkep, kapali = gri serit.
@Composable
fun Anahtar(
    acik: Boolean,
    onDegis: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val serit by animateColorAsState(
        targetValue = if (acik) Murekkep else SekmeZemin,
        animationSpec = tween(AppMotion.PANEL, easing = AppMotion.egri),
        label = "anahtarSeridi"
    )
    // Topuz 46 - 2x3 bosluk - 21 = 19 dp kayiyor.
    val kayma by animateDpAsState(
        targetValue = if (acik) 19.dp else 0.dp,
        animationSpec = tween(AppMotion.PANEL, easing = AppMotion.egri),
        label = "anahtarTopuzu"
    )

    Box(
        modifier = modifier
            .size(width = 46.dp, height = 27.dp)
            .clip(AppShape.cip)
            .background(serit)
            // Role.Switch: ekran okuyucu bunu "acik/kapali anahtar" diye okuyor.
            .toggleable(value = acik, role = Role.Switch, onValueChange = onDegis)
            .padding(3.dp)
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(kayma.roundToPx(), 0) }
                .size(21.dp)
                .shadow(2.dp, AppShape.cip)
                .background(KartZemin, AppShape.cip)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AnahtarPreview() {
    MotorumTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Anahtar(acik = false, onDegis = {})
            Anahtar(acik = true, onDegis = {})
        }
    }
}
