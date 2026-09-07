package com.oguzhanp.motorum.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oguzhanp.motorum.model.Kayit
import com.oguzhanp.motorum.ui.theme.DurumYesilMetin
import com.oguzhanp.motorum.ui.theme.DurumYesilZemin
import com.oguzhanp.motorum.ui.theme.MetinIkincil
import com.oguzhanp.motorum.ui.theme.MetinSolgun
import com.oguzhanp.motorum.ui.theme.MotorumTheme
import com.oguzhanp.motorum.util.formatBirimFiyat
import com.oguzhanp.motorum.util.formatKm
import com.oguzhanp.motorum.util.formatLitre
import com.oguzhanp.motorum.util.formatTarih
import com.oguzhanp.motorum.util.formatTl

private val KART_SEKLI = RoundedCornerShape(16.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KayitSatiri(
    kayit: Kayit,
    onTikla: () -> Unit,
    onKaydirarakSil: () -> Unit,
    modifier: Modifier = Modifier
) {
    // positionalThreshold: satir genisliginin %85'i kadar cekilmeden silinmez.
    // Kaza sonucu tetiklenmeyi engelleyen tek ayar bu.
    val kaydirmaDurumu = rememberSwipeToDismissBoxState(
        positionalThreshold = { toplamGenislik -> toplamGenislik * 0.85f }
    )

    // Kaydirma oturdugunda currentValue degisir, bu blok bir kez calisir.
    // Yon kontrolu zaten enableDismissFromStartToEnd = false ile yapiliyor.
    LaunchedEffect(kaydirmaDurumu.currentValue) {
        if (kaydirmaDurumu.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onKaydirarakSil()
        }
    }

    SwipeToDismissBox(
        state = kaydirmaDurumu,
        modifier = modifier,
        enableDismissFromStartToEnd = false,    // Saga kaydirma tamamen kapali.
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(KART_SEKLI)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        }
    ) {
        val gorunum = gorunum(kayit.kategori)

        Card(
            shape = KART_SEKLI,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTikla() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                UstSatir(kayit = kayit, gorunum = gorunum)

                val solMetin = solAlanMetni(kayit)
                val sagMetin = sagAlanMetni(kayit)

                if (solMetin != null || sagMetin.isNotBlank()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (solMetin != null) {
                            // Renk = kategori kurali bozulmasin diye devam eden yolculuga yeni renk
                            // verilmedi; normal gri hali zaten "bitmedi" demek. Sadece tamamlanan yesile doner.
                            val tamamlandi = kayit is Kayit.RoadTrip && kayit.bitis != null
                            Cip(
                                metin = solMetin,
                                zemin = if (tamamlandi) DurumYesilZemin
                                else MaterialTheme.colorScheme.outlineVariant,
                                renk = if (tamamlandi) DurumYesilMetin else MetinIkincil,
                                nokta = if (kayit is Kayit.RoadTrip) null else gorunum.renk,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        } else {
                            Box(Modifier)
                        }

                        Text(
                            text = sagMetin,
                            style = MaterialTheme.typography.bodySmall,
                            color = MetinSolgun,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UstSatir(
    kayit: Kayit,
    gorunum: KategoriGorunumu
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(11.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(gorunum.zemin),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = gorunum.ikon,
                    contentDescription = null,
                    tint = gorunum.renk,
                    modifier = Modifier.size(21.dp)
                )
            }

            Column {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = kayit.kategori.etiket,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Cip(
                        metin = gorunum.rozet,
                        zemin = gorunum.zemin,
                        renk = gorunum.metin,
                        kucuk = true
                    )
                }
                Text(
                    text = formatTarih(kayit.tarihMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = MetinIkincil
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (kayit.tutar > 0.0) {
                Text(
                    text = formatTl(kayit.tutar),
                    style = MaterialTheme.typography.titleSmall
                )
            }

            when (kayit) {
                is Kayit.Yakit -> Cip(
                    metin = formatLitre(kayit.litre),
                    zemin = gorunum.zemin,
                    renk = gorunum.metin
                )

                is Kayit.RoadTrip -> if (kayit.bitis != null) {
                    Cip(
                        metin = "↗ " + formatKm(kayit.mesafe),
                        zemin = gorunum.zemin,
                        renk = gorunum.metin
                    )
                }

                is Kayit.Bakim -> {}
                is Kayit.Aksesuar -> {}
            }
        }
    }
}

// Alt satirin kurali: solda kategoriyi tanimlayan bilgi, sagda not ya da
// turetilmis bilgi. Ikisi de when oldugu icin yeni kategoride derleyici uyarir.
private fun solAlanMetni(kayit: Kayit): String? = when (kayit) {
    is Kayit.Yakit -> kayit.not.ifBlank { null }
    is Kayit.RoadTrip -> "${kayit.baslangic.sehir} → ${kayit.bitis?.sehir ?: "devam ediyor"}"
    is Kayit.Bakim -> kayit.bakimTuru
    is Kayit.Aksesuar -> kayit.aksesuarAdi
}

private fun sagAlanMetni(kayit: Kayit): String = when (kayit) {
    is Kayit.Yakit -> formatBirimFiyat(kayit.birimFiyat)
    is Kayit.RoadTrip -> kayit.not
    is Kayit.Bakim -> kayit.not
    is Kayit.Aksesuar -> kayit.not
}

// Tek cip bileseni her yerde kullaniliyor: rozet (kucuk), litre/km, alt satir.
// nokta verilirse metnin onune kategori renginde daire cizilir.
@Composable
private fun Cip(
    metin: String,
    zemin: Color,
    renk: Color,
    modifier: Modifier = Modifier,
    kucuk: Boolean = false,
    nokta: Color? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(zemin)
            .padding(horizontal = if (kucuk) 6.dp else 9.dp, vertical = if (kucuk) 2.dp else 5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (nokta != null) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(nokta)
            )
        }
        Text(
            text = metin,
            color = renk,
            style = if (kucuk) MaterialTheme.typography.labelSmall
            else MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun KayitSatiriPreview() {
    MotorumTheme {
        KayitSatiri(
            kayit = Kayit.Yakit(
                tarihMillis = System.currentTimeMillis(),
                litre = 12.0,
                tutar = 1200.0,
                not = "SHELL"
            ),
            onTikla = {},
            onKaydirarakSil = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
