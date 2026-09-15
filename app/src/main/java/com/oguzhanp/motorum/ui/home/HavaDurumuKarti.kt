package com.oguzhanp.motorum.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oguzhanp.motorum.model.HavaDurumu
import com.oguzhanp.motorum.model.SurusDurumu
import com.oguzhanp.motorum.ui.theme.DurumYesilMetin
import com.oguzhanp.motorum.ui.theme.DurumYesilZemin
import com.oguzhanp.motorum.ui.theme.HataKirmizi
import com.oguzhanp.motorum.ui.theme.MetinAna
import com.oguzhanp.motorum.ui.theme.MetinIkincil
import com.oguzhanp.motorum.ui.theme.MetinSolgun
import com.oguzhanp.motorum.ui.theme.MotorumTheme
import com.oguzhanp.motorum.ui.theme.SurusDikkatMetin
import com.oguzhanp.motorum.ui.theme.SurusDikkatZemin
import com.oguzhanp.motorum.ui.theme.SurusKotuMetin
import com.oguzhanp.motorum.ui.theme.SurusKotuZemin
import kotlin.math.roundToInt

// Ana sayfa listesinin ilk satiri. Dort halden birini ciziyor; hangisinde
// oldugumuzu HavaDurumuViewModel soyluyor.
@Composable
fun HavaDurumuKarti(
    hal: HavaDurumuHali,
    onIzinIste: () -> Unit,
    onTekrarDene: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Zemin ve kenarlik surus durumuna bagli; o da sadece Hazir halinde var.
    // Diger hallerde kart normal beyaz kart gibi duruyor.
    val durum = (hal as? HavaDurumuHali.Hazir)?.hava?.surusDurumu

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = durum?.let { zeminRengi(it) }
                ?: MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        // Kenarlik sadece kotu havada. Uc zemin de yumusak kaliyor, gozu
        // ceken sey renk degil kenarlik ve kalin yazi.
        border = if (durum == SurusDurumu.KOTU) BorderStroke(1.5.dp, HataKirmizi) else null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            when (hal) {
                is HavaDurumuHali.Hazir -> HazirIcerik(hal.hava)
                HavaDurumuHali.Yukleniyor -> YukleniyorIcerik()
                HavaDurumuHali.Alinamadi -> AlinamadiIcerik(onTekrarDene)
                is HavaDurumuHali.IzinYok -> IzinYokIcerik(hal.izinIstendi, onIzinIste)
            }
        }
    }
}

@Composable
private fun HazirIcerik(hava: HavaDurumu) {
    val durum = hava.surusDurumu

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ikon havayi anlatiyor, renk surus durumunu. Ikisi ayri bilgi:
        // iliman bir yagmur "dikkat", karli gun "kotu" olabiliyor.
        Icon(
            imageVector = havaIkonu(hava.kod),
            contentDescription = null,
            tint = metinRengi(durum),
            modifier = Modifier.size(36.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    // Servis 23.87 gibi donduruyor, kartta tam sayi yeterli.
                    text = "${hava.sicaklik.roundToInt()}°",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MetinAna
                )
                Text(
                    text = hava.aciklama,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MetinIkincil,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            if (hava.sehir.isNotBlank()) {
                Text(
                    text = hava.sehir,
                    style = MaterialTheme.typography.bodySmall,
                    color = MetinSolgun
                )
            }

            Text(
                text = tavsiyeMetni(durum),
                style = MaterialTheme.typography.bodyMedium,
                color = metinRengi(durum),
                fontWeight = if (durum == SurusDurumu.KOTU) FontWeight.SemiBold
                else FontWeight.Normal,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun YukleniyorIcerik() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp
        )
        Text(
            text = "Hava durumu alınıyor…",
            style = MaterialTheme.typography.bodyMedium,
            color = MetinIkincil
        )
    }
}

@Composable
private fun AlinamadiIcerik(onTekrarDene: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Hava durumu alınamadı",
            style = MaterialTheme.typography.bodyMedium,
            color = MetinIkincil,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onTekrarDene) { Text("Tekrar dene") }
    }
}

@Composable
private fun IzinYokIcerik(izinIstendi: Boolean, onIzinIste: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Bulunduğun yerin hava durumu için konum izni gerekiyor",
                style = MaterialTheme.typography.bodyMedium,
                color = MetinIkincil,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onIzinIste) { Text("İzin ver") }
        }

        // Kullanici denedi ve izin gelmedi; kalici ret olabilir. Nereden
        // verilebilecegini soyluyoruz ama ayarlara YONLENDIRMIYORUZ:
        // Google bunu acikca onermiyor.
        if (izinIstendi) {
            Text(
                text = "İzni telefon ayarlarından da verebilirsin.",
                style = MaterialTheme.typography.bodySmall,
                color = MetinSolgun
            )
        }
    }
}

private fun zeminRengi(durum: SurusDurumu): Color = when (durum) {
    SurusDurumu.IYI -> DurumYesilZemin
    SurusDurumu.DIKKAT -> SurusDikkatZemin
    SurusDurumu.KOTU -> SurusKotuZemin
}

private fun metinRengi(durum: SurusDurumu): Color = when (durum) {
    SurusDurumu.IYI -> DurumYesilMetin
    SurusDurumu.DIKKAT -> SurusDikkatMetin
    SurusDurumu.KOTU -> SurusKotuMetin
}

// Cumleler burada, model/ icinde degil. Model enum donduruyor; metni
// degistirmek icin modele dokunmak gerekmiyor.
private fun tavsiyeMetni(durum: SurusDurumu): String = when (durum) {
    SurusDurumu.IYI -> "Motor için güzel bir gün"
    SurusDurumu.DIKKAT -> "Dikkatli sür"
    SurusDurumu.KOTU -> "Hava iyi değil, motoru çıkarmayabilirsin"
}

// Ayni kod araliklari model/HavaDurumu.kt icinde surus kararini veriyor;
// burada sadece hangi resmin cizilecegini soyluyorlar.
private fun havaIkonu(kod: Int): ImageVector = when {
    kod in 200..232 -> Icons.Default.Thunderstorm
    kod in 300..321 || kod in 500..531 -> Icons.Default.WaterDrop
    kod in 600..622 -> Icons.Default.AcUnit
    kod == 800 -> Icons.Default.WbSunny
    else -> Icons.Default.Cloud
}

private fun ornekHava(kod: Int, sicaklik: Double, aciklama: String, ruzgar: Double = 2.0) =
    HavaDurumu(
        sehir = "Akhisar",
        sicaklik = sicaklik,
        aciklama = aciklama,
        kod = kod,
        ruzgarHizi = ruzgar,
        gorusMesafesi = 10000
    )

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun HavaDurumuKartiPreview() {
    MotorumTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HavaDurumuKarti(
                hal = HavaDurumuHali.Hazir(ornekHava(803, 23.9, "parçalı bulutlu")),
                onIzinIste = {},
                onTekrarDene = {},
                modifier = Modifier.fillMaxWidth()
            )
            HavaDurumuKarti(
                hal = HavaDurumuHali.Hazir(ornekHava(301, 12.0, "hafif çisenti")),
                onIzinIste = {},
                onTekrarDene = {},
                modifier = Modifier.fillMaxWidth()
            )
            HavaDurumuKarti(
                hal = HavaDurumuHali.Hazir(ornekHava(502, 9.0, "kuvvetli yağmur")),
                onIzinIste = {},
                onTekrarDene = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun HavaDurumuKartiDigerHallerPreview() {
    MotorumTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HavaDurumuKarti(
                hal = HavaDurumuHali.Yukleniyor,
                onIzinIste = {},
                onTekrarDene = {},
                modifier = Modifier.fillMaxWidth()
            )
            HavaDurumuKarti(
                hal = HavaDurumuHali.Alinamadi,
                onIzinIste = {},
                onTekrarDene = {},
                modifier = Modifier.fillMaxWidth()
            )
            HavaDurumuKarti(
                hal = HavaDurumuHali.IzinYok(izinIstendi = false),
                onIzinIste = {},
                onTekrarDene = {},
                modifier = Modifier.fillMaxWidth()
            )
            HavaDurumuKarti(
                hal = HavaDurumuHali.IzinYok(izinIstendi = true),
                onIzinIste = {},
                onTekrarDene = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
