package com.oguzhanp.motorum.feature.anasayfa

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.core.tasarim.BakimMetin
import com.oguzhanp.motorum.core.tasarim.BakimZemin
import com.oguzhanp.motorum.core.tasarim.DurumYesilMetin
import com.oguzhanp.motorum.core.tasarim.DurumYesilZemin
import com.oguzhanp.motorum.core.tasarim.KategoriGorunumu
import com.oguzhanp.motorum.core.tasarim.MetinIkincil
import com.oguzhanp.motorum.core.tasarim.MetinSolgun
import com.oguzhanp.motorum.core.tasarim.MotorumIkonlari
import com.oguzhanp.motorum.core.tasarim.MotorumTheme
import com.oguzhanp.motorum.core.tasarim.basilincaKucul
import com.oguzhanp.motorum.core.tasarim.gorunum
import com.oguzhanp.motorum.core.util.formatBirimFiyat
import com.oguzhanp.motorum.core.util.formatKm
import com.oguzhanp.motorum.core.util.formatLitre
import com.oguzhanp.motorum.core.util.formatTarih
import com.oguzhanp.motorum.core.util.formatTl
import com.oguzhanp.motorum.model.HatirlatmaDurumu
import com.oguzhanp.motorum.model.Kayit

private val KART_SEKLI = RoundedCornerShape(16.dp)

@Composable
fun KayitSatiri(
    kayit: Kayit,
    onTikla: () -> Unit,
    onKaydirarakSil: () -> Unit,
    modifier: Modifier = Modifier,
    // "Zamani geldi" cipine dokunulunca: hatirlatma paneli.
    onHatirlatmaTikla: () -> Unit = {}
) {
    // Durum bilerek rememberSwipeToDismissBoxState ile degil, duz remember ile
    // kuruluyor: o surum durumu kaydedip geri yukluyor. Silinen satir "Geri al"
    // ile ayni anahtarla listeye dondugunde kaydedilmis "silindi" durumu
    // canlanip kaydi tekrar siliyordu. Burada satir yeniden kurulunca durum da
    // sifirdan basliyor.
    val kaydirmaDurumu = remember(kayit.id) {
        SwipeToDismissBoxState(
            initialValue = SwipeToDismissBoxValue.Settled,
            // positionalThreshold: satir genisliginin %85'i kadar cekilmeden
            // silinmez. Kaza sonucu tetiklenmeyi engelleyen tek ayar bu.
            positionalThreshold = { toplamGenislik -> toplamGenislik * 0.85f }
        )
    }

    SwipeToDismissBox(
        state = kaydirmaDurumu,
        modifier = modifier,
        enableDismissFromStartToEnd = false,    // Saga kaydirma tamamen kapali.
        enableDismissFromEndToStart = true,
        // Satir tamamen cekilince: yon kontrolu zaten yukarida kapali ama
        // acik kalan tek yonu burada da kontrol ediyoruz.
        onDismiss = { yon ->
            if (yon == SwipeToDismissBoxValue.EndToStart) onKaydirarakSil()
        },
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(KART_SEKLI)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = MotorumIkonlari.Sil,
                    contentDescription = stringResource(R.string.sil),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        }
    ) {
        val gorunum = gorunum(kayit.kategori)
        // Basilinca kart hafifce kuculuyor ve golgesi iniyor (tasarim: Dokunma).
        // Card'in onClick'li surumu: dokunma dalgasi kartin koselerine uyuyor.
        val etkilesim = remember { MutableInteractionSource() }

        Card(
            onClick = onTikla,
            interactionSource = etkilesim,
            shape = KART_SEKLI,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 0.dp),
            modifier = Modifier
                .basilincaKucul(etkilesim)
                .fillMaxWidth()
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
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
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
                            }

                            if (kayit is Kayit.Bakim) {
                                HatirlatmaCipi(kayit = kayit, onTikla = onHatirlatmaTikla)
                            }
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
                // Kategorinin yanindaki ek rozet (SERVIS, SURUS...) kaldirildi:
                // ikon ve renk zaten kategoriyi soyluyor, ust filtre de ayni adi yaziyor.
                Text(
                    text = stringResource(kayit.kategori.ad),
                    style = MaterialTheme.typography.titleSmall
                )
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

// Hatirlatmanin uc hali, uc renk:
// - Ileride: gri zil + tarih. Saat yok, yoksa cip satira sigmiyor.
// - Zamani geldi: kehribar (bakim rengi), dokunulabilir; panel aciliyor.
// - Yapildi: yesil onay + yapildigi gun.
@Composable
private fun HatirlatmaCipi(kayit: Kayit.Bakim, onTikla: () -> Unit) {
    when (kayit.hatirlatmaDurumu()) {
        null -> {}

        HatirlatmaDurumu.ILERIDE -> Cip(
            metin = formatTarih(kayit.hatirlatmaMillis!!),
            zemin = MaterialTheme.colorScheme.outlineVariant,
            renk = MetinIkincil,
            ikon = MotorumIkonlari.Bildirim
        )

        HatirlatmaDurumu.ZAMANI_GELDI -> Cip(
            metin = stringResource(R.string.zamani_geldi),
            zemin = BakimZemin,
            renk = BakimMetin,
            ikon = MotorumIkonlari.Bildirim,
            // Kartin geri kalani detayi aciyor; sadece cip paneli.
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onTikla)
        )

        HatirlatmaDurumu.YAPILDI -> Cip(
            metin = stringResource(
                R.string.hatirlatma_yapildi,
                formatGunKisa(kayit.hatirlatmaYapildiMillis!!)
            ),
            zemin = DurumYesilZemin,
            renk = DurumYesilMetin,
            ikon = MotorumIkonlari.Onay
        )
    }
}

// "15.06": cipte yil yer kapliyor, zaten yakin bir tarih.
private fun formatGunKisa(millis: Long): String = formatTarih(millis).substring(0, 5)

// Alt satirin kurali: solda kategoriyi tanimlayan bilgi, sagda not ya da
// turetilmis bilgi. Ikisi de when oldugu icin yeni kategoride derleyici uyarir.
@Composable
@ReadOnlyComposable
private fun solAlanMetni(kayit: Kayit): String? = when (kayit) {
    is Kayit.Yakit -> kayit.not.ifBlank { null }
    // Bitis sehri yoksa yolculuk suruyor demek.
    is Kayit.RoadTrip ->
        "${kayit.baslangic.sehir} → ${kayit.bitis?.sehir ?: stringResource(R.string.yolculuk_devam)}"
    is Kayit.Bakim -> kayit.bakimTuru
    is Kayit.Aksesuar -> kayit.aksesuarAdi
}

private fun sagAlanMetni(kayit: Kayit): String = when (kayit) {
    is Kayit.Yakit -> formatBirimFiyat(kayit.birimFiyat)
    is Kayit.RoadTrip -> kayit.not
    is Kayit.Bakim -> kayit.not
    is Kayit.Aksesuar -> kayit.not
}

// Tek cip bileseni her yerde kullaniliyor: litre/km ve alt satir.
// nokta verilirse metnin onune kategori renginde daire, ikon verilirse kucuk
// bir simge cizilir. Ikisi birden kullanilmiyor.
@Composable
private fun Cip(
    metin: String,
    zemin: Color,
    renk: Color,
    modifier: Modifier = Modifier,
    nokta: Color? = null,
    ikon: ImageVector? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(zemin)
            .padding(horizontal = 9.dp, vertical = 5.dp),
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
        if (ikon != null) {
            Icon(
                imageVector = ikon,
                contentDescription = null,
                tint = renk,
                modifier = Modifier.size(13.dp)
            )
        }
        Text(
            text = metin,
            color = renk,
            style = MaterialTheme.typography.labelMedium,
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

// Hatirlatmasi olan bakim kaydi: alt satirda bakim turu cipinin yaninda
// alarm cipi duruyor.
@Preview(showBackground = true)
@Composable
private fun KayitSatiriHatirlatmaliPreview() {
    MotorumTheme {
        KayitSatiri(
            kayit = Kayit.Bakim(
                tarihMillis = System.currentTimeMillis(),
                tutar = 1250.0,
                not = "Servis",
                bakimTuru = "Yağ değişimi",
                hatirlatmaMillis = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000
            ),
            onTikla = {},
            onKaydirarakSil = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

// Hatirlatma cipinin uc hali alt alta: ileride (gri), zamani geldi (kehribar),
// yapildi (yesil). Renkleri tek bakista karsilastirmak icin.
@Preview(showBackground = true)
@Composable
private fun KayitSatiriHatirlatmaDurumlariPreview() {
    val gun = 24L * 60 * 60 * 1000
    val simdi = System.currentTimeMillis()
    MotorumTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf(
                Kayit.Bakim(tarihMillis = simdi, tutar = 1250.0, bakimTuru = "Yağ değişimi", hatirlatmaMillis = simdi + 30 * gun),
                Kayit.Bakim(tarihMillis = simdi - 90 * gun, tutar = 900.0, bakimTuru = "Zincir bakımı", hatirlatmaMillis = simdi - gun),
                Kayit.Bakim(tarihMillis = simdi - 120 * gun, tutar = 1400.0, bakimTuru = "Fren balatası", hatirlatmaMillis = simdi - 10 * gun, hatirlatmaYapildiMillis = simdi - 2 * gun)
            ).forEach { kayit ->
                KayitSatiri(kayit = kayit, onTikla = {}, onKaydirarakSil = {})
            }
        }
    }
}
