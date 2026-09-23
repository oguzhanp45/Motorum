package com.oguzhanp.motorum.feature.belge

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.core.tasarim.AppElevation
import com.oguzhanp.motorum.core.tasarim.AppShape
import com.oguzhanp.motorum.core.tasarim.AppSpacing
import com.oguzhanp.motorum.model.Belge
import com.oguzhanp.motorum.model.BelgeTuru
import com.oguzhanp.motorum.core.tasarim.MotorumIkonlari
import com.oguzhanp.motorum.core.tasarim.BakimMetin
import com.oguzhanp.motorum.core.tasarim.BakimZemin
import com.oguzhanp.motorum.core.tasarim.CizgiSolgun
import com.oguzhanp.motorum.core.tasarim.Inter
import com.oguzhanp.motorum.core.tasarim.KartZemin
import com.oguzhanp.motorum.core.tasarim.MetinAna
import com.oguzhanp.motorum.core.tasarim.MetinEtiket
import com.oguzhanp.motorum.core.tasarim.MetinIkincil
import com.oguzhanp.motorum.core.tasarim.MetinSolgun
import com.oguzhanp.motorum.core.tasarim.MotorumTheme
import com.oguzhanp.motorum.core.tasarim.Murekkep
import com.oguzhanp.motorum.core.tasarim.YakitMetin
import com.oguzhanp.motorum.core.tasarim.YakitRenk
import com.oguzhanp.motorum.core.tasarim.YakitZemin
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Istatistikler sayfasindaki Belgeler karti (tasarim: Belgeler ①②④).
// Suresi dolanlar ustte kirmizi blok, digerleri bitisi yakin olandan uzaga,
// hic eklenmemis tekil turler en altta yer tutucu olarak.
@Composable
fun BelgelerKarti(
    durum: BelgelerUiState,
    onEkle: () -> Unit,
    onBelgeTikla: (Belge) -> Unit,
    onBosTurTikla: (BelgeTuru) -> Unit,
    modifier: Modifier = Modifier
) {
    val dolanlar = durum.belgeler.filter { it.kalanGun() < 0 }.sortedBy { it.bitisMillis }
    val gecerliler = durum.belgeler.filter { it.kalanGun() >= 0 }.sortedBy { it.bitisMillis }
    // Tekil turlerden hic eklenmemis olanlar: kullanici ne koyabilecegini gorsun.
    val eksikler = BelgeTuru.entries.filter { tur -> tur.tekil && durum.belgeler.none { it.tur == tur } }

    Card(
        shape = AppShape.kart,
        colors = CardDefaults.cardColors(containerColor = KartZemin),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.kart),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.kartIci),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.orta)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(MotorumIkonlari.Klasor, contentDescription = null, tint = Murekkep, modifier = Modifier.size(16.dp))
                Text(
                    text = stringResource(R.string.belgeler),
                    style = TextStyle(fontFamily = Inter, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold),
                    color = MetinAna,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(CizgiSolgun)
                        .clickable(onClick = onEkle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(MotorumIkonlari.Ekle, contentDescription = stringResource(R.string.belge_ekle), tint = Murekkep, modifier = Modifier.size(14.dp))
                }
            }

            when {
                durum.yukleniyor && durum.belgeler.isEmpty() -> KucukNot(stringResource(R.string.yukleniyor))
                durum.hata != null && durum.belgeler.isEmpty() -> KucukNot(durum.hata)
                else -> {
                    dolanlar.forEach { DolanBelge(it, onYeniTarih = { onBelgeTikla(it) }) }
                    gecerliler.forEach { BelgeSatiri(it, onTikla = { onBelgeTikla(it) }) }
                    eksikler.forEach { BosSatir(it, onTikla = { onBosTurTikla(it) }) }
                }
            }
        }
    }
}

// Kalan sure renkle konusuyor: 30 gunden fazla notr, 30-8 gun amber,
// 7 gun ve alti gulkurusu ve hafifce nefes aliyor.
@Composable
private fun BelgeSatiri(belge: Belge, onTikla: () -> Unit) {
    val kalan = belge.kalanGun()
    val (renk, zemin) = when {
        kalan <= 7 -> YakitMetin to YakitZemin
        kalan <= 30 -> BakimMetin to BakimZemin
        else -> MetinEtiket to CizgiSolgun
    }
    Satir(
        ikon = belge.tur.ikon(),
        ikonRengi = renk,
        ikonZemini = zemin,
        ad = belge.gorunenAdi(),
        alt = stringResource(R.string.belge_bitiyor, tamTarih(belge.bitisMillis) + yilEki(belge.bitisMillis)),
        onTikla = onTikla
    ) {
        Text(
            text = kalanYazisi(kalan),
            style = TextStyle(fontFamily = Inter, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.2).sp),
            color = if (kalan > 30) MetinIkincil else renk,
            modifier = if (kalan <= 7) Modifier.nabiz() else Modifier
        )
    }
}

@Composable
private fun BosSatir(tur: BelgeTuru, onTikla: () -> Unit) {
    Satir(
        ikon = tur.ikon(),
        ikonRengi = MetinSolgun,
        ikonZemini = CizgiSolgun,
        ad = stringResource(tur.ad),
        alt = stringResource(R.string.tarih_eklenmedi),
        onTikla = onTikla
    ) {}
}

// Suresi dolan belge: kirmizi blok ve tek eylem. Panik yaratmadan ama
// gormezden gelinemeyecek sekilde.
@Composable
private fun DolanBelge(belge: Belge, onYeniTarih: () -> Unit) {
    val once = -belge.kalanGun()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(YakitZemin)
            .border(1.5.dp, YakitRenk.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            IkonKutusu(MotorumIkonlari.Uyari, YakitMetin, KartZemin)
            Column {
                Text(
                    text = stringResource(R.string.belge_suresi_doldu, belge.gorunenAdi()),
                    style = TextStyle(fontFamily = Inter, fontSize = 12.5.sp, fontWeight = FontWeight.Bold),
                    color = YakitMetin
                )
                Text(
                    text = stringResource(
                        R.string.dolan_belge_alt,
                        if (once == 1) stringResource(R.string.dun) else pluralStringResource(R.plurals.gun_once, once, once),
                        tamTarih(belge.bitisMillis)
                    ),
                    style = TextStyle(fontFamily = Inter, fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                    color = YakitMetin.copy(alpha = 0.8f)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(11.dp))
                .background(KartZemin)
                .clickable(onClick = onYeniTarih)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(MotorumIkonlari.Takvim, contentDescription = null, tint = YakitMetin, modifier = Modifier.size(15.dp))
            Text(
                text = stringResource(R.string.yeni_tarihi_gir),
                style = TextStyle(fontFamily = Inter, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold),
                color = YakitMetin
            )
        }
    }
}

@Composable
private fun Satir(
    ikon: ImageVector,
    ikonRengi: Color,
    ikonZemini: Color,
    ad: String,
    alt: String,
    onTikla: () -> Unit,
    sag: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onTikla),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IkonKutusu(ikon, ikonRengi, ikonZemini)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ad,
                style = TextStyle(fontFamily = Inter, fontSize = 12.5.sp, fontWeight = FontWeight.Bold),
                color = MetinAna
            )
            Text(
                text = alt,
                style = TextStyle(fontFamily = Inter, fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                color = MetinSolgun
            )
        }
        sag()
    }
}

@Composable
private fun IkonKutusu(ikon: ImageVector, renk: Color, zemin: Color) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(zemin),
        contentAlignment = Alignment.Center
    ) {
        Icon(ikon, contentDescription = null, tint = renk, modifier = Modifier.size(15.dp))
    }
}

@Composable
private fun KucukNot(metin: String) {
    Text(
        text = metin,
        style = TextStyle(fontFamily = Inter, fontSize = 11.sp, fontWeight = FontWeight.Medium),
        color = MetinSolgun
    )
}

// Yaklasan belgenin kalan gunu yavasca soluklasip geri geliyor. Deger sadece
// cizim katmaninda okunuyor: satir her karede yeniden kurulmuyor.
@Composable
private fun Modifier.nabiz(): Modifier {
    val alfa = rememberInfiniteTransition(label = "nabiz").animateFloat(
        initialValue = 1f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "nabizAlfa"
    )
    return this.graphicsLayer { alpha = alfa.value }
}

// Kategori ikonlarindan bilerek farkli: kasko semsiye, sigorta muhurlu belge,
// muayene panolu tik. "Diger" icin genel belge (klasor).
private fun BelgeTuru.ikon(): ImageVector = when (this) {
    BelgeTuru.SIGORTA -> MotorumIkonlari.MuhurluBelge
    BelgeTuru.MUAYENE -> MotorumIkonlari.PanoluTik
    BelgeTuru.KASKO -> MotorumIkonlari.Semsiye
    BelgeTuru.DIGER -> MotorumIkonlari.Klasor
}

// Belgenin ekranda gorunen adi: "Diger" turde kullanicinin yazdigi ad,
// digerlerinde turun cevrilmis adi.
@Composable
@ReadOnlyComposable
private fun Belge.gorunenAdi(): String =
    if (tur == BelgeTuru.DIGER) ad else stringResource(tur.ad)

// 0 -> "Bugün", 60 gunden azsa gun, fazlaysa ay: "42 gün", "8 ay".
@Composable
@ReadOnlyComposable
private fun kalanYazisi(kalan: Int): String = when {
    kalan == 0 -> stringResource(R.string.kalan_bugun)
    kalan < 60 -> pluralStringResource(R.plurals.kalan_gun, kalan, kalan)
    else -> pluralStringResource(R.plurals.kalan_ay, kalan / 30, kalan / 30)
}

// Ay adi ve sira uygulamanin diline gore: Turkce "12 Haziran 2026",
// Ingilizce "12 June 2026".
private fun tamTarih(millis: Long): String =
    SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date(millis))

// "2026'da", "2027'de", "2033'te": ek yilin okunusunun son sesine gore
// seciliyor. Yil sayisi sifirla bitiyorsa onlar basamagi, o da sifirsa
// "bin"/"yuz" okunusu belirliyor. Sadece Turkce'de: diger dillerde cumle
// "expires 12 June 2026" gibi eksiz kuruluyor.
private fun yilEki(millis: Long): String {
    if (Locale.getDefault().language != "tr") return ""
    val yil = Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.YEAR)
    val birler = yil % 10
    val onlar = (yil / 10) % 10
    val ek = when {
        birler != 0 -> listOf("", "de", "de", "te", "te", "te", "da", "de", "de", "da")[birler]
        onlar != 0 -> listOf("", "da", "de", "da", "ta", "de", "ta", "te", "de", "da")[onlar]
        else -> "de" // 2000 "bin", 2100 "yuz": ikisi de "de"
    }
    return "'$ek"
}

@Preview(showBackground = true)
@Composable
private fun BelgelerKartiPreview() {
    val gun = 24L * 60 * 60 * 1000
    val simdi = System.currentTimeMillis()
    MotorumTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            BelgelerKarti(
                durum = BelgelerUiState(
                    belgeler = listOf(
                        Belge(tur = BelgeTuru.SIGORTA, bitisMillis = simdi - 3 * gun),
                        Belge(tur = BelgeTuru.MUAYENE, bitisMillis = simdi + 42 * gun),
                        Belge(tur = BelgeTuru.DIGER, ad = "Ehliyet", bitisMillis = simdi + 5 * gun)
                    ),
                    yukleniyor = false
                ),
                onEkle = {}, onBelgeTikla = {}, onBosTurTikla = {}
            )
            BelgelerKarti(
                durum = BelgelerUiState(yukleniyor = false),
                onEkle = {}, onBelgeTikla = {}, onBosTurTikla = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF07090E)
@Composable
private fun BelgelerKartiKaranlikPreview() {
    val gun = 24L * 60 * 60 * 1000
    val simdi = System.currentTimeMillis()
    MotorumTheme(karanlik = true) {
        BelgelerKarti(
            durum = BelgelerUiState(
                belgeler = listOf(
                    Belge(tur = BelgeTuru.SIGORTA, bitisMillis = simdi + 5 * gun),
                    Belge(tur = BelgeTuru.MUAYENE, bitisMillis = simdi + 20 * gun),
                    Belge(tur = BelgeTuru.KASKO, bitisMillis = simdi + 240 * gun)
                ),
                yukleniyor = false
            ),
            onEkle = {}, onBelgeTikla = {}, onBosTurTikla = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
