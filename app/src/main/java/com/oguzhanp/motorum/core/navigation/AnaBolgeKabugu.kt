package com.oguzhanp.motorum.core.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.oguzhanp.motorum.core.tasarim.MotorumTheme

// Uc sekmenin ortak iskeleti: ust bar, alt bar ve kayan buton burada.
// Her sayfa kendi basligini ve icerigini veriyor. Kayit Ekle / Detay bu kabugu
// kullanmiyor, o yuzden onlarda alt bar hic cizilmiyor.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnaBolgeKabugu(
    baslik: String,
    seciliRota: String,
    onSekmeTikla: (String) -> Unit,
    ustBarAksiyonlari: @Composable RowScope.() -> Unit = {},
    kayanButon: @Composable () -> Unit = {},
    // Scaffold snackbar'i alt barin ve kayan butonun ustune kendisi
    // yerlestiriyor; elle konumlandirmaya calismak yanlis olurdu.
    snackbarAlani: @Composable () -> Unit = {},
    icerik: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(baslik) },
                actions = ustBarAksiyonlari,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = { AltBar(seciliRota = seciliRota, onSekmeTikla = onSekmeTikla) },
        floatingActionButton = kayanButon,
        snackbarHost = snackbarAlani,
        content = icerik
    )
}

// Kabugun kendisi onizleniyor: ust bar, alt bar ve icerigin birakilan
// bosluga oturusu. Icerik yerine tek satir metin yeterli.
@Preview(showBackground = true)
@Composable
private fun AnaBolgeKabuguPreview() {
    MotorumTheme {
        AnaBolgeKabugu(
            baslik = "Motorum",
            seciliRota = Routes.ANA_SAYFA,
            onSekmeTikla = {}
        ) { icPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(icPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Sayfa içeriği buraya geliyor")
            }
        }
    }
}
