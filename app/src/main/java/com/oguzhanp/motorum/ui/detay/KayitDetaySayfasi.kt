package com.oguzhanp.motorum.ui.detay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.navigation.NavController
import com.oguzhanp.motorum.core.constants.AppSpacing
import com.oguzhanp.motorum.model.Kayit
import com.oguzhanp.motorum.model.TripNoktasi
import com.oguzhanp.motorum.ui.form.KayitFormu
import com.oguzhanp.motorum.ui.form.RoadTripAlanlari
import com.oguzhanp.motorum.ui.form.TripNoktasiFormu
import com.oguzhanp.motorum.ui.form.YakitAlanlari
import com.oguzhanp.motorum.ui.home.KayitViewModel
import com.oguzhanp.motorum.ui.theme.MotorumTheme
import com.oguzhanp.motorum.util.dakikaAl
import com.oguzhanp.motorum.util.saatAl
import com.oguzhanp.motorum.util.tarihSaatBirlestir

@Composable
fun KayitDetaySayfasi(
    kayitViewModel: KayitViewModel,
    navController: NavController,
    kayitId: String
) {
    val uiState by kayitViewModel.uiState.collectAsStateWithLifecycle()
    val kayit = uiState.kayitlar.find { it.id == kayitId }

    if (kayit == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    var detay by remember(kayit.id) {
        mutableStateOf(
            KayitDetayUiState(
                form = when (kayit) {
                    is Kayit.Yakit -> KayitFormu.Yakit(
                        tarihMillis = kayit.tarihMillis,
                        litreYazi = kayit.litre.toString(),
                        tutarYazi = kayit.tutar.toString(),
                        not = kayit.not
                    )

                    is Kayit.RoadTrip -> KayitFormu.RoadTrip(
                        baslangic = TripNoktasiFormu(
                            tarihMillis = kayit.baslangic.tarihMillis,
                            saat = saatAl(kayit.baslangic.tarihMillis),
                            dakika = dakikaAl(kayit.baslangic.tarihMillis),
                            kmYazi = kayit.baslangic.km.toString(),
                            sehir = kayit.baslangic.sehir
                        ),
                        bitis = kayit.bitis?.let {
                            TripNoktasiFormu(
                                tarihMillis = it.tarihMillis,
                                saat = saatAl(it.tarihMillis),
                                dakika = dakikaAl(it.tarihMillis),
                                kmYazi = it.km.toString(),
                                sehir = it.sehir
                            )
                        } ?: TripNoktasiFormu(),
                        molalar = kayit.molalar,
                        masrafYazi = if (kayit.tutar > 0.0) kayit.tutar.toString() else "",
                        not = kayit.not
                    )
                }
            )
        )
    }

    KayitDetayIcerik(
        form = detay.form,
        silmeOnayiGoster = detay.silmeOnayiGoster,
        onFormDegis = { yeniForm -> detay = detay.copy(form = yeniForm) },
        onSilmeOnayiDegis = { goster -> detay = detay.copy(silmeOnayiGoster = goster) },
        onSil = {
            navController.popBackStack() // Once geri don, sonra sil. Tersi olsaydi liste guncellenince ekran bir kez daha
            kayitViewModel.sil(kayitId) // cizilir, find null doner ve ustteki LaunchedEffect ikinci bir popBackStack cagirirdi.
        },
        onGeri = { navController.popBackStack() },
        onGuncelle = {
            val kontrol = detay.form.dogrula()
            if (kontrol.gecerli) {
                // Form gecerliyse kayit nesnesi uretiliyor. litre/tutar sadece Yakit'te
                // oldugu icin tip daraltmadan erisilemiyor.
                when (kontrol) {
                    is KayitFormu.Yakit -> kayitViewModel.duzenle(
                        Kayit.Yakit(
                            id = kayitId,
                            tarihMillis = kontrol.tarihMillis,
                            litre = kontrol.litre!!,
                            tutar = kontrol.tutar!!,
                            not = kontrol.not.trim()
                        )
                    )

                    is KayitFormu.RoadTrip -> kayitViewModel.duzenle(
                        Kayit.RoadTrip(
                            id = kayitId,
                            tutar = kontrol.masraf,
                            not = kontrol.not.trim(),
                            baslangic = TripNoktasi(
                                tarihMillis = tarihSaatBirlestir(
                                    kontrol.baslangic.tarihMillis!!,
                                    kontrol.baslangic.saat!!,
                                    kontrol.baslangic.dakika!!
                                ),
                                km = kontrol.baslangic.km!!,
                                sehir = kontrol.baslangic.sehir.trim()
                            ),
                            // Bitis bolumu bos birakildiysa yolculuk devam ediyor: null yaziliyor.
                            bitis = if (kontrol.bitisVar) TripNoktasi(
                                tarihMillis = tarihSaatBirlestir(
                                    kontrol.bitis.tarihMillis!!,
                                    kontrol.bitis.saat!!,
                                    kontrol.bitis.dakika!!
                                ),
                                km = kontrol.bitis.km!!,
                                sehir = kontrol.bitis.sehir.trim()
                            ) else null,
                            molalar = kontrol.doluMolalar
                        )
                    )
                }
                navController.popBackStack()
            } else {
                detay = detay.copy(form = kontrol)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KayitDetayIcerik(
    form: KayitFormu,
    silmeOnayiGoster: Boolean,
    onFormDegis: (KayitFormu) -> Unit,
    onSilmeOnayiDegis: (Boolean) -> Unit,// Diyalogu acmak ve iptal etmek ayni islem.true/false yapmak.
    onSil: () -> Unit,
    onGuncelle: () -> Unit,
    onGeri: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kayıt Detayı") },
                navigationIcon = {
                    IconButton(onClick = onGeri) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { onSilmeOnayiDegis(true) }) {
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
            }

            OutlinedTextField(
                value = form.not,
                onValueChange = { onFormDegis(form.notDegistir(it)) },
                label = { Text("Not (isteğe bağlı)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = onGuncelle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Güncelle")
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
                TextButton(onClick = onSil) { Text("Sil") }
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
            onFormDegis = {},
            onSilmeOnayiDegis = {},
            onSil = {},
            onGuncelle = {},
            onGeri = {}
        )
    }
}
