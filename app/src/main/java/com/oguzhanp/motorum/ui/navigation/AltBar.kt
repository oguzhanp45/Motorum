package com.oguzhanp.motorum.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.core.constants.AppMotion
import com.oguzhanp.motorum.core.constants.AppShape
import com.oguzhanp.motorum.ui.components.MotorumIkonlari
import com.oguzhanp.motorum.ui.theme.CizgiSolgun
import com.oguzhanp.motorum.ui.theme.Inter
import com.oguzhanp.motorum.ui.theme.KartZemin
import com.oguzhanp.motorum.ui.theme.MetinSolgun
import com.oguzhanp.motorum.ui.theme.MotorumTheme
import com.oguzhanp.motorum.ui.theme.Murekkep

// etiket metnin kendisi degil kimligi: ekranda hangi dilde yazilacagina
// stringResource karar veriyor.
enum class Sekme(val rota: String, @StringRes val etiket: Int, val ikon: ImageVector) {
    MOTORLARIM(Routes.MOTORLARIM, R.string.sekme_motorlarim, MotorumIkonlari.Motor),
    ANA_SAYFA(Routes.ANA_SAYFA, R.string.sekme_ana_sayfa, MotorumIkonlari.Ev),
    AYARLAR(Routes.AYARLAR, R.string.sekme_ayarlar, MotorumIkonlari.Ayarlar)
}

// Alt bar (tasarim: ana ekranlarin altindaki serit). Secili sekme gri bir
// hap: ikon ve adi yan yana. Digerleri sadece soluk ikon. Material'in
// NavigationBar'i her sekmenin altina yazi koyuyor ve boyu daha yuksek;
// tasarimin sade gorunumu icin kendi seridimizi ciziyoruz.
@Composable
fun AltBar(
    seciliRota: String,
    onSekmeTikla: (String) -> Unit
) {
    Column(modifier = Modifier.background(KartZemin)) {
        HorizontalDivider(color = CizgiSolgun)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // Telefonun alt gezinme cubugu seridin ustune binmesin.
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                // Ekran okuyucu uc sekmeyi tek bir grup olarak okusun.
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Sekme.entries.forEach { sekme ->
                SekmeDugmesi(
                    sekme = sekme,
                    secili = seciliRota == sekme.rota,
                    onTikla = { onSekmeTikla(sekme.rota) }
                )
            }
        }
    }
}

// Secim degisince eski hap soluklasip daraliyor, yenisi genisleyip beliriyor:
// goz, secimin bir sekmeden digerine gectigini goruyor.
@Composable
private fun SekmeDugmesi(sekme: Sekme, secili: Boolean, onTikla: () -> Unit) {
    val etiket = stringResource(sekme.etiket)
    val gecis = tween<Color>(AppMotion.PANEL, easing = AppMotion.egri)
    val zemin by animateColorAsState(if (secili) CizgiSolgun else Color.Transparent, gecis, label = "sekmeZemini")
    val renk by animateColorAsState(if (secili) Murekkep else MetinSolgun, gecis, label = "sekmeRengi")
    // Tasarimda secili hapin yan boslugu 14, digerlerininki 18: o da yumusak degisiyor.
    val yanBosluk by animateDpAsState(
        if (secili) 14.dp else 18.dp,
        tween(AppMotion.PANEL, easing = AppMotion.egri),
        label = "sekmeBoslugu"
    )

    Row(
        modifier = Modifier
            .clip(AppShape.cip)
            .background(zemin)
            .selectable(selected = secili, role = Role.Tab, onClick = onTikla)
            .padding(horizontal = yanBosluk, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = sekme.ikon,
            // Yazi gorunmuyorken ekran okuyucu sekmenin adini ikondan okuyor.
            contentDescription = if (secili) null else etiket,
            tint = renk,
            modifier = Modifier.size(if (secili) 19.dp else 20.dp)
        )
        AnimatedVisibility(
            visible = secili,
            enter = expandHorizontally(tween(AppMotion.PANEL, easing = AppMotion.egri)) + fadeIn(tween(AppMotion.PANEL)),
            exit = shrinkHorizontally(tween(AppMotion.PANEL, easing = AppMotion.egri)) + fadeOut(tween(AppMotion.PANEL))
        ) {
            Text(
                text = etiket,
                style = TextStyle(fontFamily = Inter, fontSize = 12.sp, fontWeight = FontWeight.Bold),
                color = Murekkep,
                maxLines = 1,
                modifier = Modifier.padding(start = 7.dp)
            )
        }
    }
}

// Ortadaki sekme secili: secili ve secili olmayan halleri birlikte gorunuyor.
@Preview(showBackground = true)
@Composable
private fun AltBarPreview() {
    MotorumTheme {
        AltBar(seciliRota = Routes.ANA_SAYFA, onSekmeTikla = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF07090E)
@Composable
private fun AltBarKaranlikPreview() {
    MotorumTheme(karanlik = true) {
        AltBar(seciliRota = Routes.AYARLAR, onSekmeTikla = {})
    }
}
