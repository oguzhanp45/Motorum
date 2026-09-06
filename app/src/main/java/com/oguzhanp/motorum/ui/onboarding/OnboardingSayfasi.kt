package com.oguzhanp.motorum.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oguzhanp.motorum.core.constants.AppSpacing
import com.oguzhanp.motorum.ui.theme.MotorumTheme
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
@Composable
fun OnboardingSayfasi(
    onOnboardingBitti: () -> Unit,
    modifier: Modifier = Modifier
) {
    val durum = rememberPagerState(pageCount = { SAYFALAR.size })
    val kapsam = rememberCoroutineScope()
    val sonSayfa = durum.currentPage == SAYFALAR.lastIndex

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppSpacing.normal)
    ) {
        // Yukseklik sabit: son sayfada buton kalkinca duzen ziplamasin.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (!sonSayfa) {
                TextButton(onClick = onOnboardingBitti) { Text("Geç") }
            }
        }

        HorizontalPager(
            state = durum,
            modifier = Modifier.weight(1f)
        ) { sira ->
            OnboardingIcerik(sayfa = SAYFALAR[sira])
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SayfaGostergesi(durum = durum)

            Button(
                onClick = {
                    if (sonSayfa) {
                        onOnboardingBitti()
                    } else {
                        kapsam.launch { durum.animateScrollToPage(durum.currentPage + 1) }
                    }
                }
            ) {
                Text(if (sonSayfa) "Başla" else "İleri")
            }
        }
    }
}

@Composable
private fun OnboardingIcerik(
    sayfa: OnboardingSayfa,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.normal),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = sayfa.baslik,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = sayfa.aciklama,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

// Kendi state'i yok, durumu pager'dan okuyor. Tek gercek kaynak durum.currentPage:
// hem parmakla kaydirma hem Ileri butonu ayni degeri degistiriyor.
@Composable
private fun SayfaGostergesi(
    durum: PagerState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(durum.pageCount) { sira ->
            val secili = durum.currentPage == sira
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (secili) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingSayfasiPreview() {
    MotorumTheme {
        OnboardingSayfasi(onOnboardingBitti = {})
    }
}
