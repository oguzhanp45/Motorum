package com.oguzhanp.motorum.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AnaSayfaIcerik(
        uiState = uiState,
        onEkleTikla = { navController.navigate(Routes.KAYIT_EKLE) },
        onKayitTikla = { id -> navController.navigate("kayit_detay/$id") }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnaSayfaIcerik(
    uiState: KayitUiState,
    onEkleTikla: () -> Unit,
    onKayitTikla: (String) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onEkleTikla) {
                Icon(Icons.Default.Add, contentDescription = "Kayıt ekle")
            }
        }
    ) { innerPadding ->
        // innerPadding UYGULANMAK ZORUNDA: yoksa icerik ust barin altinda kalir
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ToplamCard(
                toplamTutar = uiState.toplamTutar,
                toplamLitre = uiState.toplamLitre,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Kayıtlar", style = MaterialTheme.typography.titleMedium)

            // LazyColumn sadece gorunen satirlari cizer.
            // key = { it.id } -> satirlari kimlikle takip eder.
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(
                    uiState.kayitlar.sortedByDescending { it.tarihMillis },
                    key = { it.id }) { kayit ->
                    KayitSatiri(
                        kayit = kayit,
                        onTikla = { onKayitTikla(kayit.id) },
                        //KayitSatiri — Modifier.clickable { onTikla() }.
                        // Satır hangi kaydın olduğunu bilir ama nereye gidileceğini bilmez;
                        //onTikla = { onKayitTikla(kayit.id) } ile id'yi yukarı taşır.
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
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
            onKayitTikla = {}
        )
    }
}
