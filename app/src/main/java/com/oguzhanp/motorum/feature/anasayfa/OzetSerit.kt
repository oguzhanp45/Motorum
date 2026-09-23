package com.oguzhanp.motorum.feature.anasayfa

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.core.tasarim.AppElevation
import com.oguzhanp.motorum.core.tasarim.AppShape
import com.oguzhanp.motorum.core.tasarim.AppSpacing
import com.oguzhanp.motorum.core.tasarim.MotorumIkonlari
import com.oguzhanp.motorum.core.tasarim.MetinSolgun
import com.oguzhanp.motorum.core.tasarim.MotorumTheme
import com.oguzhanp.motorum.core.util.formatKm
import com.oguzhanp.motorum.core.util.formatLitre
import com.oguzhanp.motorum.core.util.formatTl

// ToplamCard'in kaydirma halindeki karsiligi: ayni uc sayi, tek satirda.
// Kart tamamen kaybolmuyor cunku rakamlar listeyi okurken de isimize yariyor;
// sadece yer kaplamayi birakiyor.
@Composable
fun OzetSerit(
    toplamTutar: Double,
    toplamLitre: Double,
    gidilenYol: Int,
    onTikla: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onTikla,
        shape = AppShape.kart,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.kart),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.normal, vertical = AppSpacing.orta),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.kucuk)
        ) {
            Deger(formatTl(toplamTutar))
            Ayirici()
            Deger(formatLitre(toplamLitre))
            Ayirici()
            Deger(formatKm(gidilenYol))

            Box(modifier = Modifier.weight(1f))

            Icon(
                imageVector = MotorumIkonlari.Cevron,
                contentDescription = stringResource(R.string.istatistik_detayi),
                tint = MetinSolgun,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun Deger(metin: String) {
    Text(
        text = metin,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

// Nokta, dikey cizgiden daha sessiz: serit zaten dar, ince cizgiler onu
// bolunmus gosteriyordu.
@Composable
private fun Ayirici() {
    Text(
        text = "·",
        style = MaterialTheme.typography.bodyMedium,
        color = MetinSolgun
    )
}

@Preview(showBackground = true)
@Composable
private fun OzetSeritPreview() {
    MotorumTheme {
        OzetSerit(
            toplamTutar = 4730.0,
            toplamLitre = 45.0,
            gidilenYol = 1050,
            onTikla = {},
            modifier = Modifier.padding(AppSpacing.normal)
        )
    }
}
