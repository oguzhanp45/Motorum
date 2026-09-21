package com.oguzhanp.motorum.ui.form

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oguzhanp.motorum.core.constants.AppMotion
import com.oguzhanp.motorum.core.constants.AppShape
import com.oguzhanp.motorum.ui.components.Anahtar
import com.oguzhanp.motorum.ui.components.MotorumIkonlari
import com.oguzhanp.motorum.ui.ekle.components.SaatDiyalogu
import com.oguzhanp.motorum.ui.ekle.components.TarihDiyalogu
import com.oguzhanp.motorum.ui.theme.Inter
import com.oguzhanp.motorum.ui.theme.Kenar
import com.oguzhanp.motorum.ui.theme.MetinAna
import com.oguzhanp.motorum.ui.theme.MetinIkincil
import com.oguzhanp.motorum.ui.theme.MetinSolgun
import com.oguzhanp.motorum.ui.theme.MotorumTheme
import com.oguzhanp.motorum.ui.theme.Murekkep
import com.oguzhanp.motorum.ui.theme.MurekkepUstu
import com.oguzhanp.motorum.ui.theme.RoadTripMetin
import com.oguzhanp.motorum.ui.theme.SekmeZemin
import com.oguzhanp.motorum.ui.theme.UyariMetin
import com.oguzhanp.motorum.ui.theme.Zemin
import com.oguzhanp.motorum.util.formatGunAy
import com.oguzhanp.motorum.util.formatSaat
import com.oguzhanp.motorum.util.formatTarih

// Formdaki hatirlatma blogu (tasarim: Hatirlatma ①②). Kapaliyken tek satir,
// acilinca asagi dogru genisliyor: hazir aralik cipleri + tarih ve saat.
// Bakim'a bagli degil, sadece degerleri aliyor; Belgeler geldiginde ayni
// blok orada da kullanilabilir.
@Composable
fun HatirlatmaBlogu(
    baslik: String,
    acik: Boolean,
    // Secili hazir aralik (ay). null = Ozel.
    secilenAy: Int?,
    tarihMillis: Long,
    saat: Int?,
    dakika: Int?,
    // Kayitli ve zamani gecmis hatirlatma: calmis, ozet soluk gorunuyor.
    gecmis: Boolean,
    hatali: Boolean,
    bildirimIzniVar: Boolean,
    onAnahtar: (Boolean) -> Unit,
    onAralikSec: (Int) -> Unit,
    onTarihSec: (Long) -> Unit,
    onSaatSec: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Takvim hangi gunle acilsin: Ozel'den bugunle, tarih alanindan secili gunle.
    var tarihAcik by remember { mutableStateOf<Long?>(null) }
    var saatAcik by remember { mutableStateOf(false) }

    // Acikken cerceve murekkebe donuyor ve disina ince bir hale cikiyor:
    // odaklanmis bir alan gibi. Kapaliyken diger alanlarla ayni gri.
    val cerceve by animateColorAsState(
        targetValue = if (acik) Murekkep else Kenar,
        animationSpec = tween(AppMotion.PANEL, easing = AppMotion.egri),
        label = "hatirlatmaCercevesi"
    )
    val hale = Murekkep.copy(alpha = if (acik) 0.07f else 0f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            // Hale cercevenin DISINDA ciziliyor; boylece blok acilip kapanirken
            // genisligi degismiyor, yandaki alanlarla hizasi bozulmuyor.
            .drawBehind {
                val kalinlik = 3.dp.toPx()
                drawRoundRect(
                    color = hale,
                    topLeft = Offset(-kalinlik / 2, -kalinlik / 2),
                    size = Size(size.width + kalinlik, size.height + kalinlik),
                    cornerRadius = CornerRadius(14.dp.toPx() + kalinlik / 2),
                    style = Stroke(kalinlik)
                )
            }
            .border(1.5.dp, cerceve, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SallananZil(sallansin = acik, renk = if (acik) Murekkep else MetinSolgun)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = baslik,
                    style = TextStyle(fontFamily = Inter, fontSize = 13.5.sp, fontWeight = FontWeight.Bold),
                    color = MetinAna
                )
                Text(
                    text = if (acik) ozet(secilenAy, tarihMillis, saat, dakika, gecmis) else "Kapalı",
                    style = TextStyle(
                        fontFamily = Inter,
                        fontSize = 10.5.sp,
                        fontWeight = if (acik) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    // Turkuaz = kurulu ve ileride. Kehribar = yeni secilen an gecmiste.
                    color = when {
                        !acik -> MetinSolgun
                        hatali -> UyariMetin
                        gecmis -> MetinSolgun
                        else -> RoadTripMetin
                    }
                )
            }
            Anahtar(acik = acik, onDegis = onAnahtar)
        }

        AnimatedVisibility(
            visible = acik,
            enter = expandVertically(tween(AppMotion.PANEL, easing = AppMotion.egri), expandFrom = Alignment.Top) +
                    fadeIn(tween(AppMotion.PANEL)),
            exit = shrinkVertically(tween(AppMotion.PANEL, easing = AppMotion.egri), shrinkTowards = Alignment.Top) +
                    fadeOut(tween(AppMotion.PANEL))
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HATIRLATMA_ARALIKLARI.forEach { ay ->
                        AralikCipi(
                            metin = "$ay ay",
                            secili = secilenAy == ay,
                            onTikla = { onAralikSec(ay) }
                        )
                    }
                    // Ozel: takvimi bugunle aciyor, secilen gun "Ozel" oluyor.
                    AralikCipi(
                        metin = "Özel",
                        secili = secilenAy == null,
                        onTikla = { tarihAcik = System.currentTimeMillis() }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    SecimAlani(
                        ikon = MotorumIkonlari.Takvim,
                        metin = formatTarih(tarihMillis),
                        onTikla = { tarihAcik = tarihMillis },
                        modifier = Modifier.weight(1f)
                    )
                    SecimAlani(
                        ikon = MotorumIkonlari.Saat,
                        metin = if (saat != null && dakika != null) formatSaat(saat, dakika) else "--:--",
                        onTikla = { saatAcik = true }
                    )
                }

                if (hatali) {
                    Text(
                        text = "Hatırlatma için gelecekte bir tarih ve saat seç",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Izin yoksa tarih yine kaydediliyor ve kartta gorunuyor, sadece
                // bildirim gelmiyor. Kullanicinin bunu tam burada bilmesi gerekiyor.
                if (!bildirimIzniVar) {
                    Text(
                        text = "Bildirim izni verilmedi. Tarih kaydedilecek ama hatırlatma gelmeyecek.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MetinIkincil
                    )
                }
            }
        }
    }

    tarihAcik?.let { baslangic ->
        TarihDiyalogu(
            tarihMillis = baslangic,
            onTarihSec = onTarihSec,
            onKapat = { tarihAcik = null }
        )
    }
    if (saatAcik) {
        SaatDiyalogu(
            saat = saat,
            dakika = dakika,
            onSaatSec = onSaatSec,
            onKapat = { saatAcik = false }
        )
    }
}

// "3 ay sonra · 12 Haziran, 10:00" ya da Ozel'de sadece "12 Haziran, 10:00".
private fun ozet(ay: Int?, tarihMillis: Long, saat: Int?, dakika: Int?, gecmis: Boolean): String {
    val zaman = formatGunAy(tarihMillis) +
            if (saat != null && dakika != null) ", " + formatSaat(saat, dakika) else ""
    return when {
        gecmis -> "$zaman · hatırlatıldı"
        ay != null -> "$ay ay sonra · $zaman"
        else -> zaman
    }
}

// Zil acikken arada bir sallaniyor: "bu kurulu, seni uyaracak". Maketteki
// canSalla animasyonu: 4 saniyenin son dortte birinde kisa bir titreme.
// Aci sadece cizim katmaninda okunuyor, satir her karede yeniden kurulmuyor.
@Composable
private fun SallananZil(sallansin: Boolean, renk: Color) {
    val aci = if (sallansin) {
        val gecis = rememberInfiniteTransition(label = "zil")
        gecis.animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 4000
                    // Eksi degerler parantezde: satir basindaki "-" bir onceki
                    // satirin devami gibi okunmasin.
                    0f at 2880
                    (-12f) at 3120
                    10f at 3360
                    (-5f) at 3600
                    0f at 4000
                },
                repeatMode = RepeatMode.Restart
            ),
            label = "zilAcisi"
        )
    } else {
        null
    }

    Icon(
        imageVector = MotorumIkonlari.Bildirim,
        contentDescription = null,
        tint = renk,
        modifier = Modifier
            .size(19.dp)
            .graphicsLayer {
                // Zil tepesinden asili gibi donuyor.
                transformOrigin = TransformOrigin(0.5f, 0f)
                rotationZ = aci?.value ?: 0f
            }
    )
}

@Composable
private fun AralikCipi(metin: String, secili: Boolean, onTikla: () -> Unit) {
    Text(
        text = metin,
        style = TextStyle(
            fontFamily = Inter,
            fontSize = 11.sp,
            fontWeight = if (secili) FontWeight.ExtraBold else FontWeight.Bold
        ),
        color = if (secili) MurekkepUstu else MetinIkincil,
        modifier = Modifier
            .clip(AppShape.cip)
            .background(if (secili) Murekkep else Color.Transparent)
            .border(1.4.dp, if (secili) Murekkep else SekmeZemin, AppShape.cip)
            .clickable(onClick = onTikla)
            .padding(horizontal = 11.dp, vertical = 6.dp)
    )
}

// Ikonlu kucuk alan: dokununca takvim ya da saat diyalogu aciliyor.
@Composable
private fun SecimAlani(
    ikon: ImageVector,
    metin: String,
    onTikla: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sekil = RoundedCornerShape(11.dp)
    Row(
        modifier = modifier
            .clip(sekil)
            .background(Zemin)
            .border(1.5.dp, Kenar, sekil)
            .clickable(onClick = onTikla)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(ikon, contentDescription = null, tint = MetinIkincil, modifier = Modifier.size(15.dp))
        Text(
            text = metin,
            style = TextStyle(fontFamily = Inter, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold),
            color = MetinAna
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HatirlatmaBloguPreview() {
    MotorumTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HatirlatmaBlogu(
                baslik = "Bakım zamanı hatırlat", acik = false, secilenAy = null,
                tarihMillis = System.currentTimeMillis(), saat = null, dakika = null,
                gecmis = false, hatali = false, bildirimIzniVar = true,
                onAnahtar = {}, onAralikSec = {}, onTarihSec = {}, onSaatSec = { _, _ -> }
            )
            HatirlatmaBlogu(
                baslik = "Bakım zamanı hatırlat", acik = true, secilenAy = 3,
                tarihMillis = System.currentTimeMillis() + 90L * 24 * 60 * 60 * 1000,
                saat = 10, dakika = 0,
                gecmis = false, hatali = false, bildirimIzniVar = true,
                onAnahtar = {}, onAralikSec = {}, onTarihSec = {}, onSaatSec = { _, _ -> }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF07090E)
@Composable
private fun HatirlatmaBloguKaranlikPreview() {
    MotorumTheme(karanlik = true) {
        HatirlatmaBlogu(
            baslik = "Bakım zamanı hatırlat", acik = true, secilenAy = 3,
            tarihMillis = System.currentTimeMillis() + 90L * 24 * 60 * 60 * 1000,
            saat = 10, dakika = 0,
            gecmis = false, hatali = false, bildirimIzniVar = false,
            onAnahtar = {}, onAralikSec = {}, onTarihSec = {}, onSaatSec = { _, _ -> },
            modifier = Modifier.padding(16.dp)
        )
    }
}
