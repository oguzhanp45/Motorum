package com.oguzhanp.motorum.ui.detay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.oguzhanp.motorum.core.constants.AppSpacing
import com.oguzhanp.motorum.model.Kayit
import com.oguzhanp.motorum.model.TripNoktasi
import com.oguzhanp.motorum.ui.form.AksesuarAlanlari
import com.oguzhanp.motorum.ui.form.BakimAlanlari
import com.oguzhanp.motorum.ui.form.KayitFormu
import com.oguzhanp.motorum.ui.form.RoadTripAlanlari
import com.oguzhanp.motorum.ui.form.YakitAlanlari
import com.oguzhanp.motorum.ui.home.KayitViewModel
import com.oguzhanp.motorum.ui.home.gorunum
import com.oguzhanp.motorum.ui.theme.MotorumTheme
import com.oguzhanp.motorum.util.tarihSaatBirlestir

@Composable
fun KayitDetaySayfasi(
    kayitViewModel: KayitViewModel,
    navController: NavController,
    kayitId: String,
    detayViewModel: KayitDetayViewModel = hiltViewModel()
) {
    val kayitUiState by kayitViewModel.uiState.collectAsStateWithLifecycle()
    val kayit = kayitUiState.kayitlar.find { it.id == kayitId }

    if (kayit == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    LaunchedEffect(kayit.id) { detayViewModel.baslat(kayit) }

    val detay by detayViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(detay.bitti) {
        if (detay.bitti) navController.popBackStack()
    }

    KayitDetayIcerik(
        form = detay.form,
        silmeOnayiGoster = detay.silmeOnayiGoster,
        calisiyor = detay.calisiyor,
        hata = detay.hata,
        onFormDegis = detayViewModel::formDegis,
        onSilmeOnayiDegis = detayViewModel::silmeOnayiDegis,
        onSilOnayla = { detayViewModel.sil(kayitId) },
        onGeriTikla = { navController.popBackStack() },
        onGuncelleTikla = {
            val kontrol = detay.form.dogrula()
            if (kontrol.gecerli) {
                // Form gecerliyse kayit nesnesi uretiliyor. litre/tutar sadece Yakit'te
                // oldugu icin tip daraltmadan erisilemiyor.
                val guncel: Kayit = when (kontrol) {
                    is KayitFormu.Yakit -> Kayit.Yakit(
                        id = kayitId,
                        tarihMillis = kontrol.tarihMillis,
                        litre = kontrol.litre!!,
                        tutar = kontrol.tutar!!,
                        not = kontrol.not.trim()
                    )

                    is KayitFormu.RoadTrip -> Kayit.RoadTrip(
                        id = kayitId,
                        tutar = kontrol.masraf,
                        not = kontrol.not.trim(),
                        baslangic = TripNoktasi(
                            tarihMillis = tarihSaatBirlestir(
                                kontrol.baslangic.tarihMillis,
                                kontrol.baslangic.saat,
                                kontrol.baslangic.dakika
                            ),
                            km = kontrol.baslangic.km!!,
                            sehir = kontrol.baslangic.sehir.trim()
                        ),
                        // Bitis bolumu bos birakildiysa yolculuk devam ediyor: null yaziliyor.
                        bitis = if (kontrol.bitisVar) TripNoktasi(
                            tarihMillis = tarihSaatBirlestir(
                                kontrol.bitis.tarihMillis,
                                kontrol.bitis.saat,
                                kontrol.bitis.dakika
                            ),
                            km = kontrol.bitis.km!!,
                            sehir = kontrol.bitis.sehir.trim()
                        ) else null,
                        molalar = kontrol.doluMolalar
                    )

                    is KayitFormu.Bakim -> Kayit.Bakim(
                        id = kayitId,
                        tarihMillis = kontrol.tarihMillis,
                        tutar = kontrol.tutar!!,
                        not = kontrol.not.trim(),
                        bakimTuru = kontrol.bakimTuru.trim()
                    )

                    is KayitFormu.Aksesuar -> Kayit.Aksesuar(
                        id = kayitId,
                        tarihMillis = kontrol.tarihMillis,
                        tutar = kontrol.tutar!!,
                        not = kontrol.not.trim(),
                        aksesuarAdi = kontrol.aksesuarAdi.trim()
                    )
                }
                detayViewModel.guncelle(guncel)
            } else {
                detayViewModel.formDegis(kontrol)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KayitDetayIcerik(
    form: KayitFormu,
    silmeOnayiGoster: Boolean,
    calisiyor: Boolean,
    hata: String?,
    onFormDegis: (KayitFormu) -> Unit,
    onSilmeOnayiDegis: (Boolean) -> Unit,// Diyalogu acmak ve iptal etmek ayni islem.true/false yapmak.
    onSilOnayla: () -> Unit,
    onGuncelleTikla: () -> Unit,
    onGeriTikla: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kayıt Detayı") },
                navigationIcon = {
                    IconButton(onClick = onGeriTikla) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onSilmeOnayiDegis(true) },
                        enabled = !calisiyor
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Sil")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.normal),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Kategori: ${form.kategori.etiket}",
                style = MaterialTheme.typography.bodyLarge
            )

            // Alan blogu kategoriye gore secilir. Yeni kategori eklendiginde
            // derleyici bu when'in eksik oldugunu gosterir.
            when (form) {
                is KayitFormu.Yakit -> YakitAlanlari(
                    form = form,
                    onDegis = onFormDegis,
                    modifier = Modifier.fillMaxWidth()
                )

                is KayitFormu.RoadTrip -> RoadTripAlanlari(
                    form = form,
                    onDegis = onFormDegis,
                    molaGoster = true,
                    bitisGoster = true,
                    modifier = Modifier.fillMaxWidth()
                )

                is KayitFormu.Bakim -> BakimAlanlari(
                    form = form,
                    onDegis = onFormDegis,
                    modifier = Modifier.fillMaxWidth()
                )

                is KayitFormu.Aksesuar -> AksesuarAlanlari(
                    form = form,
                    onDegis = onFormDegis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = form.not,
                onValueChange = { onFormDegis(form.notDegistir(it)) },
                label = { Text(gorunum(form.kategori).notEtiketi) },
                modifier = Modifier.fillMaxWidth()
            )

            if (hata != null) {
                Text(
                    text = hata,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = onGuncelleTikla,
                enabled = !calisiyor,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (calisiyor) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Güncelle")
                }
            }
        }
    }

    // Compose'da diyalog gosterilmez, VAR ya da YOK olur.
    // Kosul true iken AlertDialog eklenir, false iken hic cizilmez.
    if (silmeOnayiGoster) {
        AlertDialog(
            // Disari tiklama ve geri tusu buraya duser.
            // Burada false yazmazsak diyalog kapanmaz.
            onDismissRequest = { onSilmeOnayiDegis(false) },
            title = { Text("Kaydı sil") },
            text = { Text("Bu kaydı silmek istediğinize emin misiniz?") },
            confirmButton = {
                TextButton(onClick = onSilOnayla) { Text("Sil") }
            },
            dismissButton = {
                TextButton(onClick = { onSilmeOnayiDegis(false) }) { Text("İptal") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun KayitDetayIcerikPreview() {
    MotorumTheme {
        KayitDetayIcerik(
            form = KayitFormu.Yakit(),
            silmeOnayiGoster = false,
            calisiyor = false,
            hata = null,
            onFormDegis = {},
            onSilmeOnayiDegis = {},
            onSilOnayla = {},
            onGuncelleTikla = {},
            onGeriTikla = {}
        )
    }
}
