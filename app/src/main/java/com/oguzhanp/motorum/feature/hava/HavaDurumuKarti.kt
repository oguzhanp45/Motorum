package com.oguzhanp.motorum.feature.hava

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.core.tasarim.AppShape
import com.oguzhanp.motorum.core.tasarim.AppSpacing
import com.oguzhanp.motorum.core.tasarim.BaglantiMavi
import com.oguzhanp.motorum.core.tasarim.MetinIkincil
import com.oguzhanp.motorum.core.tasarim.MetinSolgun
import com.oguzhanp.motorum.core.tasarim.MotorumIkonlari
import com.oguzhanp.motorum.core.tasarim.MotorumTheme
import com.oguzhanp.motorum.core.tasarim.karanlikTema
import com.oguzhanp.motorum.core.util.uygulamaAyarlariniAc
import com.oguzhanp.motorum.model.HavaDurumu
import com.oguzhanp.motorum.model.SurusDurumu
import kotlin.math.roundToInt

// Hava seridi. Tek satir: ikon, sicaklik, aciklama, sehir.
//
// Surus durumunu metin degil RENK anlatiyor: hava bozdukca serit koyulasiyor
// ve hareketleniyor. Iyi gunde acik mavi ve sakin, dikkat gerektiren gunde
// koyu mavi ve yagmurlu, kotu gunde gece rengi ve simsekli.
//
// Adi hala "Kart", cagiran yerler degismesin diye; gorunusu artik serit.
@Composable
fun HavaDurumuKarti(
    hal: HavaDurumuHali,
    onIzinIste: () -> Unit,
    onTekrarDene: () -> Unit,
    modifier: Modifier = Modifier
) {
    val baglam = LocalContext.current

    when (hal) {
        is HavaDurumuHali.Hazir -> HazirSerit(hal.hava, modifier)

        HavaDurumuHali.Yukleniyor -> BilgiSeridi(
            ikon = null,
            metin = stringResource(R.string.hava_aliniyor),
            eylem = null,
            onEylem = {},
            modifier = modifier
        )

        HavaDurumuHali.Alinamadi -> BilgiSeridi(
            ikon = MotorumIkonlari.BulutYok,
            metin = stringResource(R.string.hava_alinamadi),
            eylem = stringResource(R.string.tekrar_dene_kisa),
            onEylem = onTekrarDene,
            modifier = modifier
        )

        // Ilk hal: izni uygulamanin icinden istiyoruz.
        //
        // Kullanici reddettikten sonra: Android ikinci retten sonra izin
        // penceresini bir daha GOSTERMIYOR, "Izin ver" dugmesi o noktada hicbir
        // sey yapmiyordu. Bu yuzden reddedildikten sonra dugme ayarlara
        // goturuyor. Kendiliginden yonlendirmiyoruz, kullanici basinca gidiyor;
        // Google'in kalici ret icin onerdigi yol da bu.
        is HavaDurumuHali.IzinYok -> BilgiSeridi(
            ikon = MotorumIkonlari.Konum,
            metin = stringResource(
                if (hal.izinIstendi) R.string.hava_konum_ayarlar else R.string.hava_konum_izni
            ),
            eylem = stringResource(if (hal.izinIstendi) R.string.ayarlara_git else R.string.izin_ver),
            onEylem = if (hal.izinIstendi) ({ uygulamaAyarlariniAc(baglam) }) else onIzinIste,
            modifier = modifier
        )
    }
}

@Composable
private fun HazirSerit(hava: HavaDurumu, modifier: Modifier) {
    val tur = havaTuru(hava.kod)
    val gorunum = seritGorunumu(hava.surusDurumu, tur, karanlikTema)
    val saat = rememberSaat()

    Box(
        modifier = modifier
            // Golge seridin rengini tasiyor: mavi serit mavi, koyu serit koyu
            // golge birakiyor. Gri golge renkli seridi kirli gosteriyordu.
            .shadow(
                elevation = gorunum.golge,
                shape = AppShape.serit,
                ambientColor = gorunum.golgeRengi,
                spotColor = gorunum.golgeRengi
            )
            .clip(AppShape.serit)
            .background(gorunum.zemin)
    ) {
        // Yagmur, simsek parlamasi, suzulen bulut: yazinin ARKASINDA.
        HavaParcaciklari(
            tur = tur,
            saat = saat,
            modifier = Modifier.matchParentSize(),
            bulutRengi = gorunum.bulut
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            HavaIkonu(tur = tur, renk = gorunum.ikon, saat = saat)

            Text(
                text = "${hava.sicaklik.roundToInt()}°",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                color = gorunum.sicaklik
            )

            // Aciklama esnek: uzun bir aciklama sehri itmesin, kendisi kisalsin.
            Text(
                text = hava.aciklama,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = gorunum.aciklama,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = hava.sehir,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = gorunum.sehir,
                maxLines = 1
            )
        }
    }
}

// Veri olmayan haller: beyaz, sakin bir serit ve varsa tek bir eylem.
// Hazir seritle ayni boy ve kose; halden hale gecerken yer oynamasin.
@Composable
private fun BilgiSeridi(
    ikon: ImageVector?,
    metin: String,
    eylem: String?,
    onEylem: () -> Unit,
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .shadow(elevation = 1.dp, shape = AppShape.serit)
            .clip(AppShape.serit)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.orta)
    ) {
        if (ikon != null) {
            Icon(
                imageVector = ikon,
                contentDescription = null,
                tint = MetinSolgun,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = metin,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            color = MetinIkincil,
            modifier = Modifier.weight(1f)
        )

        if (eylem != null) {
            Text(
                text = eylem,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BaglantiMavi,
                modifier = Modifier
                    .clip(AppShape.alanIci)
                    .clickable(onClick = onEylem)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}

// ------------------------------------------------------------- RENKLER

// Seridin renk takimi. Color.kt'ye tasinmadi: bu degerler sadece burada
// anlam tasiyor (gradyan duraklari, golge tonlari), baska bir bilesenin
// onlari kullanmasi yanlis olurdu.
private class SeritGorunumu(
    val zemin: Brush,
    val ikon: Color,
    val sicaklik: Color,
    val aciklama: Color,
    val sehir: Color,
    val golge: Dp,
    val golgeRengi: Color,
    val bulut: Color = Color.White
)

// Tasarimdaki 120 derecelik gradyanin karsiligi: sol ustten sag alta.
private fun gradyan(vararg duraklar: Pair<Float, Color>): Brush =
    Brush.linearGradient(
        *duraklar,
        start = Offset.Zero,
        // Sonsuz bitis "kutunun karsi kosesi" demek; boyut ne olursa olsun.
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

private val ACIK_SERIT = SeritGorunumu(
    zemin = gradyan(0f to Color(0xFFEFF6FF), 0.55f to Color(0xFFDBEAFE), 1f to Color(0xFFE0F2FE)),
    ikon = Color(0xFF1D4ED8),
    sicaklik = Color(0xFF0F172A),
    aciklama = Color(0xFF334155),
    sehir = Color(0xFF64748B),
    golge = 1.dp,
    golgeRengi = Color(0x140F172A)
)

private val PARCALI_SERIT = SeritGorunumu(
    zemin = gradyan(0f to Color(0xFFF0F9FF), 0.6f to Color(0xFFDBEAFE), 1f to Color(0xFFE2E8F0)),
    ikon = Color(0xFF1D4ED8),
    sicaklik = Color(0xFF0F172A),
    aciklama = Color(0xFF334155),
    sehir = Color(0xFF64748B),
    golge = 1.dp,
    golgeRengi = Color(0x140F172A)
)

private val DIKKAT_SERIDI = SeritGorunumu(
    zemin = gradyan(0f to Color(0xFF3B82F6), 0.55f to Color(0xFF1D4ED8), 1f to Color(0xFF1E40AF)),
    ikon = Color(0xFFDBEAFE),
    sicaklik = Color.White,
    aciklama = Color(0xFFDBEAFE),
    sehir = Color(0xFFBFDBFE),
    golge = 4.dp,
    golgeRengi = Color(0x4D1D4ED8)
)

private val KOTU_SERIT = SeritGorunumu(
    zemin = gradyan(0f to Color(0xFF334155), 0.5f to Color(0xFF1E293B), 1f to Color(0xFF0B1220)),
    ikon = Color(0xFFCBD5E1),
    sicaklik = Color.White,
    aciklama = Color(0xFFE2E8F0),
    sehir = Color(0xFF94A3B8),
    golge = 6.dp,
    golgeRengi = Color(0x610F172A)
)

// Karanlik temada iyi gun: acik mavi serit koyu ekranda fener gibi yaniyordu.
// Tasarimdaki gibi gece mavisine donuyor; dikkat ve kotu seritler zaten koyu.
private val KARANLIK_IYI_SERIT = SeritGorunumu(
    zemin = gradyan(0f to Color(0xFF1B2434), 0.62f to Color(0xFF1E3A8A), 1f to Color(0xFF10182B)),
    ikon = Color(0xFF93C5FD),
    sicaklik = Color(0xFFF1F5F9),
    aciklama = Color(0xFFCBD5E1),
    sehir = Color(0xFF94A3B8),
    golge = 4.dp,
    golgeRengi = Color(0x66000000),
    bulut = Color(0xFF7E8CA3)
)

// Rengi surus durumu seciyor. Iyi gunlerin iki tonu var: tam acik gok biraz
// daha parlak, bulutlu gok biraz daha gri.
private fun seritGorunumu(durum: SurusDurumu, tur: HavaTuru, karanlik: Boolean): SeritGorunumu = when (durum) {
    SurusDurumu.KOTU -> KOTU_SERIT
    SurusDurumu.DIKKAT -> DIKKAT_SERIDI
    SurusDurumu.IYI -> when {
        karanlik -> KARANLIK_IYI_SERIT
        tur == HavaTuru.ACIK -> ACIK_SERIT
        else -> PARCALI_SERIT
    }
}

// ------------------------------------------------------------ ONIZLEME

private fun ornekHava(sicaklik: Double, aciklama: String, kod: Int) = HavaDurumu(
    sehir = "Akhisar",
    sicaklik = sicaklik,
    aciklama = aciklama,
    kod = kod,
    ruzgarHizi = 2.0,
    gorusMesafesi = 10000
)

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun HavaDurumuKartiPreview() {
    MotorumTheme {
        Column(
            modifier = Modifier.padding(AppSpacing.normal),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.orta)
        ) {
            listOf(
                ornekHava(24.0, "açık", 800),
                ornekHava(21.0, "parçalı bulutlu", 803),
                ornekHava(13.0, "çisenti", 301),
                ornekHava(6.0, "gök gürültülü sağanak", 211)
            ).forEach { hava ->
                HavaDurumuKarti(
                    hal = HavaDurumuHali.Hazir(hava),
                    onIzinIste = {},
                    onTekrarDene = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun HavaDurumuKartiDigerHallerPreview() {
    MotorumTheme {
        Column(
            modifier = Modifier.padding(AppSpacing.normal),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.orta)
        ) {
            HavaDurumuKarti(HavaDurumuHali.Yukleniyor, {}, {}, Modifier.fillMaxWidth())
            HavaDurumuKarti(HavaDurumuHali.IzinYok(), {}, {}, Modifier.fillMaxWidth())
            HavaDurumuKarti(HavaDurumuHali.Alinamadi, {}, {}, Modifier.fillMaxWidth())
        }
    }
}

// Karanlik temada iyi hava seridi gece mavisine donuyor; dikkat ve kotu ayni.
@Preview(showBackground = true, backgroundColor = 0xFF07090E)
@Composable
private fun HavaDurumuKartiKaranlikPreview() {
    MotorumTheme(karanlik = true) {
        Column(
            modifier = Modifier.padding(AppSpacing.normal),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.orta)
        ) {
            listOf(
                ornekHava(24.0, "açık", 800),
                ornekHava(21.0, "parçalı bulutlu", 803),
                ornekHava(6.0, "gök gürültülü sağanak", 211)
            ).forEach { hava ->
                HavaDurumuKarti(
                    hal = HavaDurumuHali.Hazir(hava),
                    onIzinIste = {},
                    onTekrarDene = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
