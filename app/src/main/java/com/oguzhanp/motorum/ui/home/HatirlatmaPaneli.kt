package com.oguzhanp.motorum.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oguzhanp.motorum.core.constants.AppShape
import com.oguzhanp.motorum.core.constants.AppSpacing
import com.oguzhanp.motorum.model.HatirlatmaDurumu
import com.oguzhanp.motorum.model.Kayit
import com.oguzhanp.motorum.ui.components.MotorumIkonlari
import com.oguzhanp.motorum.ui.theme.BakimRenk
import com.oguzhanp.motorum.ui.theme.BakimZemin
import com.oguzhanp.motorum.ui.theme.Kenar
import com.oguzhanp.motorum.ui.theme.KartZemin
import com.oguzhanp.motorum.ui.theme.MetinAna
import com.oguzhanp.motorum.ui.theme.MetinIkincil
import com.oguzhanp.motorum.ui.theme.MotorumTheme
import com.oguzhanp.motorum.ui.theme.Murekkep
import com.oguzhanp.motorum.ui.theme.MurekkepUstu
import com.oguzhanp.motorum.util.dakikaAl
import com.oguzhanp.motorum.util.formatGunAy
import com.oguzhanp.motorum.util.formatSaat
import com.oguzhanp.motorum.util.formatTarih
import com.oguzhanp.motorum.util.saatAl

// Hatirlatma paneli: alttan acilan pencere. Ana sayfadaki "Zamani geldi"
// cipinden ve bildirimin govdesinden ayni sekilde aciliyor; kullanici bir
// yerde ogrendigini her yerde taniyor. Uc dugme, bildirimdekilerle ayni isi
// yapiyor; "Hatirlatmayi kapat" sadece burada var.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HatirlatmaPaneli(
    kayit: Kayit.Bakim,
    onYaptirdim: () -> Unit,
    onErtele: () -> Unit,
    onKapat: () -> Unit,
    onPaneliKapat: () -> Unit
) {
    // skipPartiallyExpanded: icerik kisa, yarim acik ara durumu gereksiz.
    ModalBottomSheet(
        onDismissRequest = onPaneliKapat,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = KartZemin
    ) {
        HatirlatmaPaneliIcerik(kayit, onYaptirdim, onErtele, onKapat)
    }
}

@Composable
private fun HatirlatmaPaneliIcerik(
    kayit: Kayit.Bakim,
    onYaptirdim: () -> Unit,
    onErtele: () -> Unit,
    onKapat: () -> Unit
) {
    val zaman = kayit.hatirlatmaMillis ?: return
    val zamaniGeldi = kayit.hatirlatmaDurumu() == HatirlatmaDurumu.ZAMANI_GELDI

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = AppSpacing.genis, end = AppSpacing.genis, bottom = AppSpacing.genis),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.kucuk)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.orta)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BakimZemin),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    MotorumIkonlari.Bildirim,
                    contentDescription = null,
                    tint = BakimRenk,
                    modifier = Modifier.size(21.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = kayit.bakimTuru + if (zamaniGeldi) " zamanı geldi" else " hatırlatması",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MetinAna
                )
                Text(
                    text = "${formatTarih(kayit.tarihMillis)} tarihli bakım · " +
                            "${formatGunAy(zaman)}, ${formatSaat(saatAl(zaman), dakikaAl(zaman))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MetinIkincil
                )
            }
        }

        Spacer(Modifier.height(AppSpacing.kucuk))

        // Ana eylem murekkep dolgulu; ikincisi cerceveli, ucuncusu sade metin.
        // Onem sirasi gorunusten okunuyor.
        Button(
            onClick = onYaptirdim,
            shape = AppShape.alan,
            colors = ButtonDefaults.buttonColors(containerColor = Murekkep, contentColor = MurekkepUstu),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            DugmeIcerigi(MotorumIkonlari.Onay, "Yaptırdım")
        }
        OutlinedButton(
            onClick = onErtele,
            shape = AppShape.alan,
            border = BorderStroke(1.5.dp, Kenar),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            DugmeIcerigi(MotorumIkonlari.Saat, "1 hafta ertele", MetinAna)
        }
        TextButton(
            onClick = onKapat,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Hatırlatmayı kapat", color = MetinIkincil)
        }
    }
}

@Composable
private fun DugmeIcerigi(ikon: ImageVector, metin: String, renk: Color? = null) {
    Icon(
        ikon,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
        tint = renk ?: LocalContentColor.current
    )
    Spacer(Modifier.width(AppSpacing.kucuk))
    Text(metin, fontWeight = FontWeight.SemiBold, color = renk ?: Color.Unspecified)
}

@Preview(showBackground = true)
@Composable
private fun HatirlatmaPaneliIcerikPreview() {
    MotorumTheme {
        HatirlatmaPaneliIcerik(
            kayit = Kayit.Bakim(
                tarihMillis = System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000,
                tutar = 1250.0,
                bakimTuru = "Yağ değişimi",
                hatirlatmaMillis = System.currentTimeMillis() - 60_000
            ),
            onYaptirdim = {}, onErtele = {}, onKapat = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF141B29)
@Composable
private fun HatirlatmaPaneliIcerikKaranlikPreview() {
    MotorumTheme(karanlik = true) {
        HatirlatmaPaneliIcerik(
            kayit = Kayit.Bakim(
                tarihMillis = System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000,
                tutar = 1250.0,
                bakimTuru = "Yağ değişimi",
                hatirlatmaMillis = System.currentTimeMillis() - 60_000
            ),
            onYaptirdim = {}, onErtele = {}, onKapat = {}
        )
    }
}
