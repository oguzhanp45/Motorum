package com.oguzhanp.motorum.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.ui.navigation.Routes
import com.oguzhanp.motorum.ui.theme.MotorumTheme


@Composable
fun AnaSayfa(
    viewModel: KayitViewModel,
    navController: NavController,
    onCikisTikla: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.yukle() }

    AnaSayfaIcerik(
        uiState = uiState,
        onEkleTikla = { navController.navigate(Routes.KAYIT_EKLE) },
        onKayitTikla = { id -> navController.navigate("kayit_detay/$id") },
        onKayitKaydirarakSil = { id -> viewModel.sil(id) },
        onYenileTikla = { viewModel.yukle() },
        onCikisTikla = onCikisTikla
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnaSayfaIcerik(
    uiState: KayitUiState,
    onEkleTikla: () -> Unit,
    onKayitTikla: (String) -> Unit,
    onKayitKaydirarakSil: (String) -> Unit,
    onYenileTikla: () -> Unit,
    onCikisTikla: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onYenileTikla, enabled = !uiState.yukleniyor) {
                        Icon(Icons.Default.Refresh, contentDescription = "Yenile")
                    }
                    IconButton(onClick = onCikisTikla) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Çıkış yap")
                    }
                }
            )
        },
        floatingActionButton = {
            // FAB varsayilan olarak primaryContainer kullaniyor, primary degil.
            // Tasarimdaki dolu mavi icin renkleri burada aciktan veriyoruz.
            FloatingActionButton(
                onClick = onEkleTikla,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Kayıt ekle")
            }
        }
    ) { innerPadding ->
        // innerPadding UYGULANMAK ZORUNDA: yoksa icerik ust barin altinda kalir
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            when {
                uiState.yukleniyor -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )

                uiState.hata != null -> HataDurumu(
                    mesaj = uiState.hata,
                    onTekrarDeneTikla = onYenileTikla,
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> Liste(
                    uiState = uiState,
                    onKayitTikla = onKayitTikla,
                    onKayitKaydirarakSil = onKayitKaydirarakSil
                )
            }
        }
    }
}

@Composable
private fun Liste(
    uiState: KayitUiState,
    onKayitTikla: (String) -> Unit,
    onKayitKaydirarakSil: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ToplamCard(
            toplamTutar = uiState.toplamTutar,
            toplamLitre = uiState.toplamLitre,
            toplamKm = uiState.toplamKm,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Kayıtlar", style = MaterialTheme.typography.titleMedium)

        // LazyColumn sadece gorunen satirlari cizer.
        // key = { it.id } -> satirlari kimlikle takip eder.
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Siralama burada, listede: depo ekleme sirasini koruyor,
            // ekran nasil gostermek istedigine kendisi karar veriyor.
            items(
                uiState.kayitlar.sortedByDescending { it.tarihMillis },
                key = { it.id }) { kayit ->
                KayitSatiri(
                    kayit = kayit,
                    onTikla = { onKayitTikla(kayit.id) },
                    onKaydirarakSil = { onKayitKaydirarakSil(kayit.id) },
                    //KayitSatiri — Modifier.clickable { onTikla() }.
                    // Satır hangi kaydın olduğunu bilir ama nereye gidileceğini bilmez;
                    //onTikla = { onKayitTikla(kayit.id) } ile id'yi yukarı taşır.
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun HataDurumu(
    mesaj: String,
    onTekrarDeneTikla: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = mesaj,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        OutlinedButton(onClick = onTekrarDeneTikla) {
            Text("Tekrar Dene")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AnaSayfaIcerikPreview() {
    MotorumTheme {
        AnaSayfaIcerik(
            uiState = KayitUiState(),
            onEkleTikla = {},
            onKayitTikla = {},
            onKayitKaydirarakSil = {},
            onYenileTikla = {},
            onCikisTikla = {}
        )
    }
}
