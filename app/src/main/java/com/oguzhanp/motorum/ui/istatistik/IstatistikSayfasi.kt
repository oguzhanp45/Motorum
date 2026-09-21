package com.oguzhanp.motorum.ui.istatistik

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.oguzhanp.motorum.core.constants.AppElevation
import com.oguzhanp.motorum.core.constants.AppShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.oguzhanp.motorum.core.constants.AppSpacing
import com.oguzhanp.motorum.ui.home.KayitViewModel
import com.oguzhanp.motorum.ui.home.gorunum
import com.oguzhanp.motorum.ui.theme.CizgiSolgun
import com.oguzhanp.motorum.ui.theme.DurumYesilMetin
import com.oguzhanp.motorum.ui.theme.MetinIkincil
import com.oguzhanp.motorum.ui.theme.MetinSolgun
import com.oguzhanp.motorum.ui.theme.MotorumTheme
import com.oguzhanp.motorum.ui.theme.YakitMetin
import com.oguzhanp.motorum.util.formatBirimFiyat
import com.oguzhanp.motorum.util.formatKm
import com.oguzhanp.motorum.util.formatKmMaliyet
import com.oguzhanp.motorum.util.formatLitre
import com.oguzhanp.motorum.util.formatTarih
import com.oguzhanp.motorum.util.formatTl
import kotlin.math.roundToInt
import com.oguzhanp.motorum.ui.components.MotorumIkonlari

@Composable
fun IstatistikSayfasi(
    kayitViewModel: KayitViewModel,
    navController: NavController
) {
    // Kendi deposu yok: ana sayfanin listesini kullaniyor. Motor degisince
    // ya da asagi cekilince o liste tazeleniyor, burasi kendiliginden guncelleniyor.
    val kayitUiState by kayitViewModel.uiState.collectAsStateWithLifecycle()

    // Donem ekranin kendi meselesi, ViewModel'e tasimadik. rememberSaveable
    // ise ekran dondurulunce secimin kaybolmasini engelliyor.
    var donem by rememberSaveable { mutableStateOf(Donem.TUMU) }

    // Hesap her yeniden cizimde degil, sadece liste ya da donem degisince
    // yapiliyor. Kayit sayisi buyudukce bu fark aciliyor.
    val durum = remember(kayitUiState.kayitlar, donem) {
        istatistikHesapla(kayitUiState.kayitlar, donem)
    }

    IstatistikIcerik(
        durum = durum,
        onDonemSec = { donem = it },
        onGeriTikla = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IstatistikIcerik(
    durum: IstatistikUiState,
    onDonemSec: (Donem) -> Unit,
    onGeriTikla: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("İstatistikler") },
                navigationIcon = {
                    IconButton(onClick = onGeriTikla) {
                        Icon(MotorumIkonlari.Geri, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { icPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(icPadding)
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.normal),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.orta)
        ) {
            DonemSecici(secili = durum.donem, onSec = onDonemSec)

            // Donem secici bos durumda da duruyor: kullanici "bu ay"da bir sey
            // yoksa "tumu"ne gecebilmeli.
            if (durum.bos) {
                BosDurum()
                return@Column
            }

            OzetKart(durum)
            KategoriKirilimi(durum)
            if (durum.toplamLitre > 0) YakitKarti(durum)
            // Tek dolumla cizgi olmaz: kart da cikmiyor.
            if (durum.yakitNoktalari.size >= 2) YakitFiyatKarti(durum)
            if (durum.gidilenYol > 0) YolKarti(durum)
        }
    }
}

// Material'in FilterChip'i yerine kendi segment seridimiz: giris ekranindaki
// sekme seridiyle ayni kalip. Secili olan murekkep dolgulu, digerleri sade.
@Composable
private fun DonemSecici(
    secili: Donem,
    onSec: (Donem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShape.alan)
            .background(CizgiSolgun)
            .padding(AppSpacing.cokKucuk),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.cokKucuk)
    ) {
        Donem.entries.forEach { donem ->
            val seciliMi = donem == secili
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(AppShape.alanIci)
                    // Secili olanin zemini onSurface: acik temada murekkep,
                    // karanlik temada acik renk olarak kendiliginden donuyor.
                    .background(
                        if (seciliMi) MaterialTheme.colorScheme.onSurface
                        else Color.Transparent
                    )
                    .clickable { onSec(donem) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = donem.etiket,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (seciliMi) FontWeight.Bold else FontWeight.Medium,
                    color = if (seciliMi) MaterialTheme.colorScheme.surface else MetinIkincil
                )
            }
        }
    }
}

@Composable
private fun OzetKart(durum: IstatistikUiState) {
    IstatistikKarti {
        Baslik("TOPLAM HARCAMA")
        Text(
            text = formatTl(durum.toplamTutar),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "${durum.kayitAdedi} kayıt · aylık ortalama ${formatTl(durum.aylikOrtalama)}",
            style = MaterialTheme.typography.bodySmall,
            color = MetinIkincil
        )
    }
}

@Composable
private fun KategoriKirilimi(durum: IstatistikUiState) {
    IstatistikKarti {
        Baslik("KATEGORİ DAĞILIMI")
        durum.paylar.forEach { pay ->
            val gorunum = gorunum(pay.kategori)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = gorunum.ikon,
                        contentDescription = null,
                        tint = gorunum.renk,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = pay.kategori.etiket,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Text(
                        text = "${pay.adet}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MetinSolgun,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                    Box(modifier = Modifier.weight(1f))
                    Text(
                        text = formatTl(pay.tutar),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                OranCubugu(oran = pay.oran, renk = gorunum.renk)
                Text(
                    // roundToInt: yuzde ondaligiyla gosterilince satir kalabalik
                    // goruniyor, burada bir basamak hassasiyet yeterli degil.
                    text = "%${(pay.oran * 100).roundToInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MetinIkincil
                )
            }
        }
    }
}

@Composable
private fun YakitKarti(durum: IstatistikUiState) {
    IstatistikKarti {
        Baslik("YAKIT")
        Satir("Toplam litre", formatLitre(durum.toplamLitre))
        Satir("Ortalama litre fiyatı", formatBirimFiyat(durum.ortalamaBirimFiyat))
        durum.enUcuzDolum?.let {
            Satir("En ucuz dolum", "${formatBirimFiyat(it.birimFiyat)} · ${formatTarih(it.tarihMillis)}")
        }
        durum.enPahaliDolum?.let {
            Satir("En pahalı dolum", "${formatBirimFiyat(it.birimFiyat)} · ${formatTarih(it.tarihMillis)}")
        }
    }
}

@Composable
private fun YakitFiyatKarti(durum: IstatistikUiState) {
    val ilk = durum.yakitNoktalari.first()
    val son = durum.yakitNoktalari.last()
    // Yuzde degisim ilk dolumdan son dolums: yakitta artis kotu haber oldugu
    // icin yukselisi gul rengiyle, dususu yesille yaziyoruz.
    val degisim = if (ilk.birimFiyat > 0) (son.birimFiyat - ilk.birimFiyat) / ilk.birimFiyat * 100 else 0.0
    val artti = degisim > 0

    IstatistikKarti {
        Baslik("LİTRE FİYATI")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formatBirimFiyat(son.birimFiyat),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(modifier = Modifier.weight(1f))
            Text(
                text = "${if (artti) "+" else ""}%${degisim.roundToInt()}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (artti) YakitMetin else DurumYesilMetin
            )
        }

        YakitFiyatGrafigi(noktalar = durum.yakitNoktalari)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formatTarih(ilk.tarihMillis),
                style = MaterialTheme.typography.bodySmall,
                color = MetinSolgun
            )
            Box(modifier = Modifier.weight(1f))
            Text(
                text = formatTarih(son.tarihMillis),
                style = MaterialTheme.typography.bodySmall,
                color = MetinSolgun
            )
        }
    }
}

@Composable
private fun YolKarti(durum: IstatistikUiState) {
    IstatistikKarti {
        Baslik("YOL")
        Satir("Gidilen yol", formatKm(durum.gidilenYol))
        if (durum.enUzunMesafe > 0) Satir("En uzun yolculuk", formatKm(durum.enUzunMesafe))
        // Yol bilindigi icin burada: ayri karta gerek yok.
        durum.kmBasiMaliyet?.let { Satir("Km başı yakıt maliyeti", formatKmMaliyet(it)) }
        Text(
            text = "Girilen sayaç değerlerinden hesaplanıyor: en yüksek okuma eksi en düşük. Motorun ömür boyu kilometresi değil, kayıt tutmaya başladığından beri gidilen yol.",
            style = MaterialTheme.typography.bodySmall,
            color = MetinSolgun
        )
    }
}

@Composable
private fun BosDurum() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.genis * 2),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.kucuk)
    ) {
        Text(
            text = "Bu dönemde kayıt yok",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Başka bir dönem seçebilir ya da yeni kayıt ekleyebilirsin.",
            style = MaterialTheme.typography.bodyMedium,
            color = MetinIkincil,
            textAlign = TextAlign.Center
        )
    }
}

// Kartlarin kabugu tek yerde: ToplamCard ile ayni kose ve yukseklik degerleri.
@Composable
private fun IstatistikKarti(icerik: @Composable () -> Unit) {
    Card(
        shape = AppShape.kart,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.kart),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.kartIci),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.orta)
        ) {
            icerik()
        }
    }
}

@Composable
private fun Baslik(metin: String) {
    Text(
        text = metin,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = MetinSolgun
    )
}

@Composable
private fun Satir(baslik: String, deger: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = baslik,
            style = MaterialTheme.typography.bodyMedium,
            color = MetinIkincil
        )
        Box(modifier = Modifier.weight(1f))
        Text(
            text = deger,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun OranCubugu(oran: Float, renk: Color) {
    // LinearProgressIndicator yerine iki kutu: gosterge degil, dagilim cubugu.
    // Renk kategoriden geliyor, temanin primary renginden degil.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(CizgiSolgun)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(oran)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(renk)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun IstatistikIcerikPreview() {
    MotorumTheme {
        IstatistikIcerik(
            durum = IstatistikUiState(),
            onDonemSec = {},
            onGeriTikla = {}
        )
    }
}
