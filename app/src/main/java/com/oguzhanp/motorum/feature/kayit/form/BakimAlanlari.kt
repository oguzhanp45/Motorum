package com.oguzhanp.motorum.feature.kayit.form

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oguzhanp.motorum.core.tasarim.AppMotion
import com.oguzhanp.motorum.core.tasarim.AppShape
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.feature.kayit.bilesenler.TarihSecici
import com.oguzhanp.motorum.core.tasarim.BakimMetin
import com.oguzhanp.motorum.core.tasarim.BakimRenk
import com.oguzhanp.motorum.core.tasarim.BakimZemin
import com.oguzhanp.motorum.core.tasarim.Inter
import com.oguzhanp.motorum.core.tasarim.KartZemin
import com.oguzhanp.motorum.core.tasarim.MetinIkincil
import com.oguzhanp.motorum.core.tasarim.MotorumTheme
import com.oguzhanp.motorum.core.tasarim.SekmeZemin
import java.util.Locale

// Bakim kategorisinin form alanlari. Hem ekleme hem detay ekrani ayni blogu cagiriyor.
@Composable
fun BakimAlanlari(
    form: KayitFormu.Bakim,
    onDegis: (KayitFormu.Bakim) -> Unit,
    bildirimIzniVar: Boolean,
    onHatirlatmaAcilsin: () -> Unit,
    modifier: Modifier = Modifier,
    // Sadece izin ister, formu degistirmez. Sablon cipi formu kendisi
    // dolduruyor; izin sonucu gelince formun ustune yazilmasin diye ayri.
    onHatirlatmaIzniIste: () -> Unit = {}
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = form.bakimTuru,
            onValueChange = { onDegis(form.copy(bakimTuru = it, bakimTuruHatali = false)) },
            label = { Text(stringResource(R.string.bakim_turu)) },
            isError = form.bakimTuruHatali,
            supportingText = { if (form.bakimTuruHatali) Text(stringResource(R.string.zorunlu_alan)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        SablonCipleri(
            yazilan = form.bakimTuru,
            onSec = { ad, onerilenAy ->
                onDegis(form.sablonSec(ad, onerilenAy))
                // Izin yoksa blok yine aciliyor ve uyari yaziyor; izni de burada
                // istiyoruz ki kullanici ayrica anahtara dokunmak zorunda kalmasin.
                if (!bildirimIzniVar) onHatirlatmaIzniIste()
            }
        )

        // Once "ne yaptirdim", sonra "ne zaman": tur ilk alan.
        TarihSecici(
            tarihMillis = form.tarihMillis,
            // Secili aralik varsa hatirlatma tarihi de onunla kayiyor.
            onTarihSec = { onDegis(form.tarihDegistir(it)) },
            etiket = stringResource(R.string.bakim_tarihi),
            aciklama = stringResource(R.string.bakim_tarihi_aciklama),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = form.tutarYazi,
            onValueChange = { onDegis(form.copy(tutarYazi = it, tutarHatali = false)) },
            label = { Text(stringResource(R.string.tutar_tl)) },
            isError = form.tutarHatali,
            supportingText = { if (form.tutarHatali) Text(stringResource(R.string.gecerli_sayi)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        HatirlatmaBlogu(
            baslik = stringResource(R.string.bakim_zamani_hatirlat),
            acik = form.hatirlatmaAcik,
            secilenAy = form.hatirlatmaAyi,
            tarihMillis = form.hatirlatmaTarihMillis,
            saat = form.hatirlatmaSaat,
            dakika = form.hatirlatmaDakika,
            // Kayitli ve dokunulmamis hatirlatmanin zamani gectiyse: calmis.
            gecmis = form.hatirlatmaGecmiste && form.hatirlatmaMillis == form.kayitliHatirlatma,
            hatali = form.hatirlatmaHatali,
            bildirimIzniVar = bildirimIzniVar,
            // Acarken izin akisi devreye giriyor, o yuzden karari ekrana
            // biraktik; alani acmak da onun isi. Kapatmak izin gerektirmiyor.
            onAnahtar = { acik ->
                if (acik) {
                    onHatirlatmaAcilsin()
                } else {
                    onDegis(form.copy(hatirlatmaAcik = false, hatirlatmaHatali = false))
                }
            },
            onAralikSec = { onDegis(form.araligiSec(it)) },
            onTarihSec = { onDegis(form.hatirlatmaTarihiSec(it)) },
            onSaatSec = { saat, dakika ->
                onDegis(
                    form.copy(
                        hatirlatmaSaat = saat,
                        hatirlatmaDakika = dakika,
                        hatirlatmaHatali = false
                    )
                )
            }
        )
    }
}

// Bakim turunun altindaki kisayol cipleri (tasarim: Fikir 3A).
// Cipin ekranda gorunen hali: adi cozulmus sablon.
private data class Sablon(val ad: String, val onerilenAy: Int)

// Bos alanda hepsi gorunuyor. Yazmaya baslayinca eslesenler kaliyor, eslesen
// yoksa satir kayboluyor. Yazilan metin bir sablonun ta kendisiyse o cip
// isaretli (kehribar + tik) ve digerleri de gorunuyor: fikir degistirilebilsin.
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SablonCipleri(yazilan: String, onSec: (String, Int) -> Unit) {
    // Cipin yazisi burada cozuluyor: arama da, kayda giren ad da ekranda
    // gorunen yaziyla ayni olsun.
    val sablonlar = BAKIM_SABLONLARI.map { Sablon(stringResource(it.ad), it.onerilenAy) }
    val arama = yazilan.trim().lowercase(TR)
    val secili = sablonlar.firstOrNull { it.ad.lowercase(TR) == arama }
    val gorunenler = when {
        arama.isEmpty() || secili != null -> sablonlar
        else -> sablonlar.filter { it.ad.lowercase(TR).contains(arama) }
    }

    AnimatedVisibility(
        visible = gorunenler.isNotEmpty(),
        enter = expandVertically(tween(AppMotion.PANEL, easing = AppMotion.egri)) + fadeIn(),
        exit = shrinkVertically(tween(AppMotion.PANEL, easing = AppMotion.egri)) + fadeOut()
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            gorunenler.forEach { sablon ->
                val secildi = sablon == secili
                Text(
                    text = if (secildi) stringResource(R.string.sablon_secili, sablon.ad) else sablon.ad,
                    style = TextStyle(
                        fontFamily = Inter,
                        fontSize = 11.sp,
                        fontWeight = if (secildi) FontWeight.ExtraBold else FontWeight.Bold
                    ),
                    color = if (secildi) BakimMetin else MetinIkincil,
                    modifier = Modifier
                        .clip(AppShape.cip)
                        .background(if (secildi) BakimZemin else KartZemin)
                        .border(1.4.dp, if (secildi) BakimRenk.copy(alpha = 0.45f) else SekmeZemin, AppShape.cip)
                        .clickable { onSec(sablon.ad, sablon.onerilenAy) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
        }
    }
}

// Turkce kucuk harf: "İ" -> "i", "I" -> "ı". Varsayilan dil Ingilizce olan
// telefonda da arama dogru eslessin.
private val TR: Locale = Locale.forLanguageTag("tr")

// Bos form: bes sablon cipi de gorunuyor.
@Preview(showBackground = true)
@Composable
private fun BakimAlanlariPreview() {
    MotorumTheme {
        BakimAlanlari(
            form = KayitFormu.Bakim(),
            onDegis = {},
            bildirimIzniVar = true,
            onHatirlatmaAcilsin = {}
        )
    }
}

// Sablon secilmis hali: "Yag degisimi" cipi isaretli, hatirlatma acik ve
// izin yokken altta uyari satiri.
@Preview(showBackground = true)
@Composable
private fun BakimAlanlariHatirlatmaliPreview() {
    MotorumTheme {
        BakimAlanlari(
            form = KayitFormu.Bakim(
                bakimTuru = "Yağ değişimi",
                tutarYazi = "1250",
                hatirlatmaAcik = true,
                hatirlatmaAyi = 3,
                hatirlatmaTarihMillis = System.currentTimeMillis() + 90L * 24 * 60 * 60 * 1000,
                hatirlatmaSaat = 10,
                hatirlatmaDakika = 0
            ),
            onDegis = {},
            bildirimIzniVar = false,
            onHatirlatmaAcilsin = {}
        )
    }
}
