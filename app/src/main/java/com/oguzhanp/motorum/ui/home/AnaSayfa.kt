package com.oguzhanp.motorum.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.ui.motorlarim.MotorCipi
import com.oguzhanp.motorum.ui.motorlarim.MotorSecimPaneli
import com.oguzhanp.motorum.ui.motorlarim.MotorSeciciUiState
import com.oguzhanp.motorum.ui.motorlarim.MotorSeciciViewModel
import com.oguzhanp.motorum.ui.navigation.AnaBolgeKabugu
import com.oguzhanp.motorum.ui.navigation.Routes
import com.oguzhanp.motorum.ui.theme.MetinIkincil
import com.oguzhanp.motorum.ui.theme.MetinSolgun
import com.oguzhanp.motorum.ui.theme.MotorumTheme


@Composable
fun AnaSayfa(
    viewModel: KayitViewModel,
    navController: NavController,
    onSekmeTikla: (String) -> Unit,
    seciciViewModel: MotorSeciciViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val secici by seciciViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarDurumu = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.yukle()
        seciciViewModel.yukle()
    }

    // Panelden motor secilince kayitlari yeniden cekiyoruz. Secim tamamlanmadan
    // cekmemek icin yine bayrak kullaniliyor.
    LaunchedEffect(secici.secimTamam) {
        if (secici.secimTamam) {
            viewModel.yukle()
            seciciViewModel.secimTuketildi()
        }
    }

    // showSnackbar askiya alinan bir fonksiyon: snackbar kapanana kadar
    // burada bekliyor ve nasil kapandigini donduruyor.
    LaunchedEffect(uiState.geriAlinabilir) {
        if (uiState.geriAlinabilir == null) return@LaunchedEffect
        val sonuc = snackbarDurumu.showSnackbar(
            message = "Kayıt silindi",
            actionLabel = "Geri Al",
            duration = SnackbarDuration.Short
        )
        if (sonuc == SnackbarResult.ActionPerformed) viewModel.geriAl()
        else viewModel.geriAlmaTuketildi()
    }

    AnaSayfaIcerik(
        uiState = uiState,
        secici = secici,
        snackbarDurumu = snackbarDurumu,
        onSekmeTikla = onSekmeTikla,
        onEkleTikla = { navController.navigate(Routes.KAYIT_EKLE) },
        onKayitTikla = { id -> navController.navigate("kayit_detay/$id") },
        onKayitKaydirarakSil = { id -> viewModel.sil(id) },
        onAsagiCek = { viewModel.yenile() },
        onTekrarDeneTikla = { viewModel.yukle() },
        onCipTikla = seciciViewModel::panelAc,
        onMotorSec = seciciViewModel::motoruSec,
        onMotorEkleTikla = {
            // Once paneli kapat, sonra git: geri donuldugunde panel acik kalmasin.
            seciciViewModel.panelKapat()
            navController.navigate(Routes.motorEkleRotasi(secilsin = true))
        },
        onPanelKapat = seciciViewModel::panelKapat
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnaSayfaIcerik(
    uiState: KayitUiState,
    secici: MotorSeciciUiState,
    snackbarDurumu: SnackbarHostState,
    onSekmeTikla: (String) -> Unit,
    onEkleTikla: () -> Unit,
    onKayitTikla: (String) -> Unit,
    onKayitKaydirarakSil: (String) -> Unit,
    onAsagiCek: () -> Unit,
    onTekrarDeneTikla: () -> Unit,
    onCipTikla: () -> Unit,
    onMotorSec: (String) -> Unit,
    onMotorEkleTikla: () -> Unit,
    onPanelKapat: () -> Unit
) {
    AnaBolgeKabugu(
        baslik = stringResource(R.string.app_name),
        seciliRota = Routes.ANA_SAYFA,
        onSekmeTikla = onSekmeTikla,
        ustBarAksiyonlari = {
            MotorCipi(motor = secici.seciliMotor, onTikla = onCipTikla)
        },
        snackbarAlani = { SnackbarHost(snackbarDurumu) },
        kayanButon = {
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
    ) { icPadding ->
        // icPadding UYGULANMAK ZORUNDA: yoksa icerik ust barin altinda ve
        // alt barin arkasinda kalir.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(icPadding)
                .padding(horizontal = 16.dp)
        ) {
            when {
                uiState.yukleniyor -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )

                uiState.hata != null -> HataDurumu(
                    mesaj = uiState.hata,
                    onTekrarDeneTikla = onTekrarDeneTikla,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Motoru olmayan kullaniciya hata gostermek yanlis olurdu:
                // ortada bir sorun yok, sadece yapmasi gereken bir adim var.
                uiState.motorYok -> MotorYokDurumu(
                    onMotorlarimaGit = { onSekmeTikla(Routes.MOTORLARIM) },
                    modifier = Modifier.align(Alignment.Center)
                )

                // Asagi cekme hareketi kaydirilabilir cocuktan geliyor, bu yuzden
                // sadece liste durumunu sariyor. Hata ve motor yok ekranlarinin
                // kendi butonlari var, orada cekmek bir sey cozmuyor.
                else -> PullToRefreshBox(
                    isRefreshing = uiState.yenileniyor,
                    onRefresh = onAsagiCek,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Liste(
                        uiState = uiState,
                        onKayitTikla = onKayitTikla,
                        onKayitKaydirarakSil = onKayitKaydirarakSil
                    )
                }
            }
        }
    }

    if (secici.panelAcik) {
        MotorSecimPaneli(
            motorlar = secici.motorlar,
            seciliMotorId = secici.seciliMotor?.id,
            onMotorSec = onMotorSec,
            onMotorEkleTikla = onMotorEkleTikla,
            onKapat = onPanelKapat
        )
    }
}

@Composable
private fun Liste(
    uiState: KayitUiState,
    onKayitTikla: (String) -> Unit,
    onKayitKaydirarakSil: (String) -> Unit
) {
    // Sayfanin tamami tek LazyColumn: ozet kart ve baslik da birer satir.
    // Boylece sayfa bastan sona tek parca kayiyor ve asagi cekme her yerden
    // calisiyor.
    // LazyColumn sadece gorunen satirlari cizer.
    // key = { it.id } -> satirlari kimlikle takip eder.
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ToplamCard(
                toplamTutar = uiState.toplamTutar,
                toplamLitre = uiState.toplamLitre,
                toplamKm = uiState.toplamKm,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text("Kayıtlar", style = MaterialTheme.typography.titleMedium)
        }

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

@Composable
private fun MotorYokDurumu(
    onMotorlarimaGit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Default.TwoWheeler,
            contentDescription = null,
            tint = MetinSolgun,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "Kayıtlar bir motora ait.\nÖnce bir motor eklemelisin.",
            style = MaterialTheme.typography.bodyMedium,
            color = MetinIkincil,
            textAlign = TextAlign.Center
        )
        Button(onClick = onMotorlarimaGit) { Text("Motorlarım'a git") }
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
            secici = MotorSeciciUiState(),
            snackbarDurumu = remember { SnackbarHostState() },
            onSekmeTikla = {},
            onEkleTikla = {},
            onKayitTikla = {},
            onKayitKaydirarakSil = {},
            onAsagiCek = {},
            onTekrarDeneTikla = {},
            onCipTikla = {},
            onMotorSec = {},
            onMotorEkleTikla = {},
            onPanelKapat = {}
        )
    }
}
