package com.oguzhanp.motorum.ui.components

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.oguzhanp.motorum.core.constants.AppShape
import com.oguzhanp.motorum.ui.theme.CizgiSolgun
import com.oguzhanp.motorum.ui.theme.MetinAna
import com.oguzhanp.motorum.ui.theme.MetinIkincil
import com.oguzhanp.motorum.ui.theme.MetinSolgun
import com.oguzhanp.motorum.ui.theme.MotorumTheme
import com.oguzhanp.motorum.ui.theme.Murekkep
import com.oguzhanp.motorum.ui.theme.MurekkepUstu
import com.oguzhanp.motorum.ui.theme.SekmeZemin

// Bos ekranlarin ortak kalibi: ustte bir gorsel, altinda baslik, aciklama
// ve tek bir eylem. Uc ekran ayni iskeleti kullaniyor ki bos durumlar
// uygulamanin her yerinde ayni sesle konussun.
//
// Gorseller hareketli ama sakin: bos ekran kullaniciyi bekletmiyor, sadece
// "burasi canli, bir sey eklemeni bekliyor" diyor.
enum class BosGorsel { MOTOR_YOK, KAYIT_YOK, BAGLANTI_YOK }

@Composable
fun BosDurum(
    gorsel: BosGorsel,
    baslik: String,
    aciklama: String?,
    eylem: String,
    eylemIkonu: ImageVector,
    onEylem: () -> Unit,
    modifier: Modifier = Modifier,
    // Ilk adimi atmaya cagiran ekranlarda dolu murekkep dugme; "tekrar dene"
    // gibi toparlayici eylemlerde cerceveli, daha sessiz dugme.
    anaEylem: Boolean = true
) {
    Column(
        modifier = modifier.padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (gorsel) {
            BosGorsel.MOTOR_YOK -> MotorYokGorseli()
            BosGorsel.KAYIT_YOK -> KayitYokGorseli()
            BosGorsel.BAGLANTI_YOK -> BaglantiYokGorseli()
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                text = baslik,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp,
                color = MetinAna,
                textAlign = TextAlign.Center
            )
            if (aciklama != null) {
                Text(
                    text = aciklama,
                    fontSize = 12.5.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Medium,
                    color = MetinIkincil,
                    textAlign = TextAlign.Center
                )
            }
        }

        EylemDugmesi(metin = eylem, ikon = eylemIkonu, ana = anaEylem, onTikla = onEylem)
    }
}

@Composable
private fun EylemDugmesi(metin: String, ikon: ImageVector, ana: Boolean, onTikla: () -> Unit) {
    val zemin = if (ana) Murekkep else MaterialTheme.colorScheme.surface
    val yazi = if (ana) MurekkepUstu else Murekkep

    Row(
        modifier = Modifier
            // Ana dugmenin golgesi var, cerceveli dugmenin yok: goz once
            // doldurulmus olana gitsin.
            .shadow(if (ana) 6.dp else 0.dp, AppShape.buton, spotColor = Murekkep.copy(alpha = 0.3f))
            .clip(AppShape.buton)
            .background(zemin)
            .then(if (ana) Modifier else Modifier.border(1.5.dp, SekmeZemin, AppShape.buton))
            .clickable(onClick = onTikla)
            .padding(horizontal = 22.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(ikon, contentDescription = null, tint = yazi, modifier = Modifier.size(17.dp))
        Text(text = metin, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = yazi)
    }
}

// ------------------------------------------------------------ GORSELLER

private val GORSEL_BOYU = 132.dp
private val YUMUSAK_GOLGE = Color(0x1F0F172A)

// Tasarimdaki "nefes": 3.4 saniyede bir yuzde dort buyuyup kuculuyor.
// Deger graphicsLayer icinde okunuyor; hareket cizimde kaliyor, ekran
// duzeni her karede yeniden hesaplanmiyor.
//
// Bu yuzden Float degil State donuyor: degeri burada okusaydik fonksiyon
// her karede yeniden calisirdi. Okuma, cagiran yerin graphicsLayer'inda.
@Composable
private fun nefesDegeri(): State<Float> =
    rememberInfiniteTransition(label = "nefes").animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1_700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "nefes"
    )

// Motor yok: yolun ustunde giden motorcu. Tasarimda burada duran bir motor
// ikonu vardi; motorcu animasyonunu koyduk cunku "ilk motorunu ekle" ani
// uygulamanin en cok karsilama yapan ekrani.
@Composable
private fun MotorYokGorseli() {
    val gecis = rememberInfiniteTransition(label = "yol")
    // Kesikli yol sola akiyor: motorcu saga bakiyor, yol altindan geriye
    // kayinca ileri gidiyormus gibi gorunuyor. 22 = bir kesik + bir bosluk.
    val akis by gecis.animateFloat(
        initialValue = 0f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(tween(1_035, easing = LinearEasing)),
        label = "akis"
    )
    val nefes = nefesDegeri()
    val kompozisyon by rememberLottieComposition(LottieCompositionSpec.RawRes(motorcuDosyasi()))

    Box(
        modifier = Modifier
            .size(GORSEL_BOYU)
            .graphicsLayer { scaleX = nefes.value; scaleY = nefes.value }
            .shadow(10.dp, CircleShape, ambientColor = YUMUSAK_GOLGE, spotColor = YUMUSAK_GOLGE)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Yol: tasarimdaki 128'lik kutudaki kavis, 132'lik daireye oturtuldu.
        // Renk cizim blogundan once aliniyor: blok icinden tema okunamaz.
        val yolRengi = SekmeZemin
        Canvas(modifier = Modifier.fillMaxSize()) {
            val o = size.width / 128f
            val yol = Path().apply {
                moveTo(14f * o, 96f * o)
                quadraticTo(64f * o, 78f * o, 114f * o, 96f * o)
            }
            drawPath(
                path = yol,
                color = yolRengi,
                style = Stroke(
                    width = 3f * o,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f * o, 8f * o), akis * o)
                )
            )
        }

        // Motorcunun tekerlekleri animasyon kutusunun yuzde 89'unda. Kutu
        // yukari biraz kaydirilinca tekerlekler yolun kavisine oturuyor.
        LottieAnimation(
            composition = kompozisyon,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-1).dp)
        )
    }
}

// Kayit yok: bos bir liste karti. Ucuncu cizgi nefes aliyor, "buraya
// satir gelecek" diye.
@Composable
private fun KayitYokGorseli() {
    val nefes = nefesDegeri()

    Column(
        modifier = Modifier
            .size(GORSEL_BOYU)
            .shadow(10.dp, RoundedCornerShape(30.dp), ambientColor = YUMUSAK_GOLGE, spotColor = YUMUSAK_GOLGE)
            .clip(RoundedCornerShape(30.dp))
            .background(MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterVertically)
    ) {
        Cubuk(62.dp, CizgiSolgun)
        Cubuk(46.dp, CizgiSolgun)
        Cubuk(74.dp, SekmeZemin, Modifier.graphicsLayer {
            scaleX = nefes.value; scaleY = nefes.value
            alpha = 0.9f + (nefes.value - 1f) * 2.5f
        })
        Cubuk(38.dp, CizgiSolgun)
    }
}

@Composable
private fun Cubuk(genislik: Dp, renk: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(genislik)
            .height(8.dp)
            .clip(CircleShape)
            .background(renk)
    )
}

// Baglanti yok: ustu cizili bulut ve disa dogru yayilan bir halka, sinyal
// arar gibi.
@Composable
private fun BaglantiYokGorseli() {
    val gecis = rememberInfiniteTransition(label = "dalga")
    val dalga by gecis.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2_600, easing = EaseOut)),
        label = "dalga"
    )

    Box(
        modifier = Modifier
            .size(GORSEL_BOYU)
            .shadow(10.dp, CircleShape, ambientColor = YUMUSAK_GOLGE, spotColor = YUMUSAK_GOLGE)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        // Halka 0.6'dan 1.5'e buyurken siliniyor.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val olcek = 0.6f + 0.9f * dalga
                    scaleX = olcek; scaleY = olcek
                    alpha = 0.5f * (1f - dalga)
                }
                .border(2.dp, MetinSolgun.copy(alpha = 0.6f), CircleShape)
        )

        Icon(
            imageVector = MotorumIkonlari.BulutYok,
            contentDescription = null,
            tint = MetinSolgun,
            modifier = Modifier.size(52.dp)
        )
    }
}

// ------------------------------------------------------------ ONIZLEME

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC, widthDp = 360, heightDp = 1300)
@Composable
private fun BosDurumPreview() {
    MotorumTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 30.dp),
            verticalArrangement = Arrangement.spacedBy(56.dp)
        ) {
            BosDurum(
                gorsel = BosGorsel.MOTOR_YOK,
                baslik = "Henüz motorun yok",
                aciklama = "İlk motorunu ekle, yakıt ve bakım harcamaların tek yerde toplansın.",
                eylem = "Motor Ekle",
                eylemIkonu = MotorumIkonlari.Ekle,
                onEylem = {}
            )
            BosDurum(
                gorsel = BosGorsel.KAYIT_YOK,
                baslik = "Bu motorda kayıt yok",
                aciklama = "Depoyu doldurduğunda ya da bakım yaptırdığında buraya ekle; aylık özet kendiliğinden oluşur.",
                eylem = "İlk Kaydı Ekle",
                eylemIkonu = MotorumIkonlari.Ekle,
                onEylem = {}
            )
            BosDurum(
                gorsel = BosGorsel.BAGLANTI_YOK,
                baslik = "İnternet bağlantısı yok",
                aciklama = "Kayıtların güvende. Bağlanınca kaldığın yerden devam edeceksin.",
                eylem = "Tekrar Dene",
                eylemIkonu = MotorumIkonlari.Yenile,
                onEylem = {},
                anaEylem = false
            )
        }
    }
}
