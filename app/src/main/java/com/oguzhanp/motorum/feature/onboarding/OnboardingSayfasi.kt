package com.oguzhanp.motorum.feature.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.oguzhanp.motorum.core.tasarim.DUGME_BASILI
import com.oguzhanp.motorum.core.tasarim.MotorumIkonlari
import com.oguzhanp.motorum.core.tasarim.basiliMurekkep
import com.oguzhanp.motorum.core.tasarim.basilincaKucul
import com.oguzhanp.motorum.core.tasarim.MetinAna
import com.oguzhanp.motorum.core.tasarim.MetinIkincil
import com.oguzhanp.motorum.core.tasarim.MotorumTheme
import com.oguzhanp.motorum.core.tasarim.Murekkep
import com.oguzhanp.motorum.core.tasarim.MurekkepUstu
import com.oguzhanp.motorum.core.tasarim.SekmeZemin
import com.oguzhanp.motorum.core.tasarim.Zemin
import com.oguzhanp.motorum.core.tasarim.karanlikTema
import kotlinx.coroutines.launch

data class OnboardingSayfa(
    val baslik: String,
    val aciklama: String
)

private val SAYFALAR = listOf(
    OnboardingSayfa(
        baslik = "Motorum'a hoş geldin",
        aciklama = "Motosikletinin tüm masraflarını tek yerde topla."
    ),
    OnboardingSayfa(
        baslik = "Kayıtlarını tut",
        aciklama = "Yakıt, bakım, aksesuar ve yolculuklarını birkaç dokunuşla ekle."
    ),
    OnboardingSayfa(
        baslik = "Toplamları gör",
        aciklama = "Ne kadar harcadığını, kaç litre yaktığını ve kaç km gittiğini anında gör."
    )
)

// Uc sayfa tek rota icinde HorizontalPager ile duruyor. Ayri rotalar yapsaydik
// parmakla kaydirma calismaz, geri tusuna uc kez basmak gerekirdi.
// Kaydirma, animasyon ve ekran donunce sayfayi hatirlama pager'dan hazir geliyor.
//
// Ust serit (Gec) ve alt serit (Geri, noktalar, Ileri) pager'in disinda:
// sayfalar kayarken onlar yerinde kaliyor.
@Composable
fun OnboardingSayfasi(
    onOnboardingBitti: () -> Unit,
    modifier: Modifier = Modifier
) {
    val durum = rememberPagerState(pageCount = { SAYFALAR.size })
    val kapsam = rememberCoroutineScope()
    val ilkSayfa = durum.currentPage == 0
    val sonSayfa = durum.currentPage == SAYFALAR.lastIndex

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Zemin)
            // Bu ekran Scaffold icinde degil; enableEdgeToEdge acik oldugu icin icerik
            // durum cubugunun altina girer. safeDrawingPadding durum cubugu, gezinme
            // cubugu ve centigi birden karsiliyor.
            .safeDrawingPadding()
    ) {
        // Son sayfada "Gec" gorunmez oluyor ama yeri duruyor: duzen ziplamasin.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, end = 10.dp)
                .height(40.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "Geç",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MetinIkincil,
                modifier = Modifier
                    .alpha(if (sonSayfa) 0f else 1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = !sonSayfa, onClick = onOnboardingBitti)
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            )
        }

        HorizontalPager(
            state = durum,
            modifier = Modifier.weight(1f)
        ) { sira ->
            // settledPage: kaydirma bitince guncelleniyor. Sahnelerin giris
            // animasyonu yarim kaydirmada degil, sayfa yerine oturunca basliyor.
            val gorunur = durum.settledPage == sira
            OnboardingIcerik(sayfa = SAYFALAR[sira]) {
                when (sira) {
                    0 -> HosGeldinSahnesi()
                    1 -> KategorilerSahnesi(gorunur = gorunur)
                    else -> ToplamlarSahnesi(gorunur = gorunur)
                }
            }
        }

        // Box, Row degil: noktalar yanlardaki butonlardan bagimsiz konumlaniyor.
        // Ilk sayfada Geri yok, noktalar sola yaslaniyor; sonraki sayfalarda
        // ortaya kayiyorlar (tasarimdaki gibi). Kayma animasyonlu, ziplamiyor.
        val noktaKonumu by animateFloatAsState(if (ilkSayfa) -1f else 0f, label = "noktalar")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 26.dp, bottom = 24.dp)
        ) {
            if (!ilkSayfa) {
                Text(
                    text = "Geri",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MetinIkincil,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            kapsam.launch { durum.animateScrollToPage(durum.currentPage - 1) }
                        }
                        .padding(horizontal = 6.dp, vertical = 11.dp)
                )
            }

            SayfaGostergesi(
                durum = durum,
                modifier = Modifier
                    .align(BiasAlignment(noktaKonumu, 0f))
                    .padding(start = 4.dp)
            )

            IleriDugmesi(
                sonSayfa = sonSayfa,
                onTikla = {
                    if (sonSayfa) {
                        onOnboardingBitti()
                    } else {
                        kapsam.launch { durum.animateScrollToPage(durum.currentPage + 1) }
                    }
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

// Sayfa: ustte sahne (kalan alanin ortasinda), altta sola yasli baslik ve aciklama.
@Composable
private fun OnboardingIcerik(
    sayfa: OnboardingSayfa,
    modifier: Modifier = Modifier,
    sahne: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            sahne()
        }
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = sayfa.baslik,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.6).sp,
                lineHeight = 1.2.em,
                color = MetinAna
            )
            Text(
                text = sayfa.aciklama,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 1.6.em,
                color = MetinIkincil
            )
        }
    }
}

// Kendi state'i yok, durumu pager'dan okuyor. Tek gercek kaynak durum.currentPage:
// hem parmakla kaydirma hem Ileri butonu ayni degeri degistiriyor.
// Secili nokta uzayip murekkep rengine donuyor; gecis animasyonlu.
@Composable
private fun SayfaGostergesi(
    durum: PagerState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(durum.pageCount) { sira ->
            val secili = durum.currentPage == sira
            val genislik by animateDpAsState(if (secili) 22.dp else 7.dp, label = "nokta")
            val renk by animateColorAsState(if (secili) Murekkep else SekmeZemin, label = "noktaRengi")
            Box(
                modifier = Modifier
                    .width(genislik)
                    .height(7.dp)
                    .clip(CircleShape)
                    .background(renk)
            )
        }
    }
}

// Hap bicimli murekkep dugme. Son sayfada "Basla" ve onay isareti.
@Composable
private fun IleriDugmesi(
    sonSayfa: Boolean,
    onTikla: () -> Unit,
    modifier: Modifier = Modifier
) {
    val golge = if (karanlikTema) Color.Black else Murekkep
    // Basilinca kuculup bir ton koyulasiyor (tasarim: Dokunma).
    val etkilesim = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .basilincaKucul(etkilesim, DUGME_BASILI)
            .shadow(6.dp, CircleShape, ambientColor = golge, spotColor = golge)
            .background(basiliMurekkep(etkilesim), CircleShape)
            .clip(CircleShape)
            .clickable(
                interactionSource = etkilesim,
                indication = ripple(),
                role = Role.Button,
                onClick = onTikla
            )
            .padding(horizontal = 22.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (sonSayfa) "Başla" else "İleri",
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = MurekkepUstu
        )
        Icon(
            imageVector = if (sonSayfa) MotorumIkonlari.Tik else MotorumIkonlari.Ileri,
            contentDescription = null,
            tint = MurekkepUstu,
            modifier = Modifier.size(15.dp)
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun OnboardingSayfasiPreview() {
    MotorumTheme(karanlik = false) {
        OnboardingSayfasi(onOnboardingBitti = {})
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun OnboardingSayfasiKaranlikPreview() {
    MotorumTheme(karanlik = true) {
        OnboardingSayfasi(onOnboardingBitti = {})
    }
}
