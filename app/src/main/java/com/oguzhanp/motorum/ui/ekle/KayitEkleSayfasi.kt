package com.oguzhanp.motorum.ui.ekle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.core.constants.AppSpacing
import com.oguzhanp.motorum.model.Kayit
import com.oguzhanp.motorum.model.TripNoktasi
import com.oguzhanp.motorum.ui.ekle.components.KategoriDropdown
import com.oguzhanp.motorum.ui.form.KayitFormu
import com.oguzhanp.motorum.ui.form.RoadTripAlanlari
import com.oguzhanp.motorum.ui.form.YakitAlanlari
import com.oguzhanp.motorum.ui.form.bosForm
import com.oguzhanp.motorum.ui.home.KayitViewModel
import com.oguzhanp.motorum.ui.theme.MotorumTheme
import com.oguzhanp.motorum.util.tarihSaatBirlestir

@Composable
fun KayitEkleSayfasi(
    kayitViewModel: KayitViewModel,
    navController: NavController,
    ekleViewModel: KayitEkleViewModel = viewModel()
) {
    val uiState by ekleViewModel.uiState.collectAsStateWithLifecycle()

    KayitEkleIcerik(
        form = uiState.form,
        onFormDegis = { yeniForm -> ekleViewModel.guncelle(uiState.copy(form = yeniForm)) },
        onGeri = { navController.popBackStack() },
        onKaydet = {
            val kontrol = uiState.form.dogrula()
            if (kontrol.gecerli) {
                // Form gecerliyse kayit nesnesi uretiliyor. litre/tutar sadece Yakit'te
                // oldugu icin tip daraltmadan erisilemiyor.
                when (kontrol) {
                    is KayitFormu.Yakit -> kayitViewModel.ekle(
                        Kayit.Yakit(
                            tarihMillis = kontrol.tarihMillis,
                            litre = kontrol.litre!!,
                            tutar = kontrol.tutar!!,
                            not = kontrol.not.trim()
                        )
                    )

                    is KayitFormu.RoadTrip -> kayitViewModel.ekle(
                        Kayit.RoadTrip(
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
                            molalar = kontrol.doluMolalar
                        )
                    )
                }
                navController.popBackStack()
            } else {
                ekleViewModel.guncelle(uiState.copy(form = kontrol))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KayitEkleIcerik(
    form: KayitFormu,
    onFormDegis: (KayitFormu) -> Unit,
    onKaydet: () -> Unit,
    onGeri: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.kayit_ekle)) },
                navigationIcon = {
                    IconButton(onClick = onGeri) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
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
            KategoriDropdown(
                secili = form.kategori,
                // Kategori degisince o kategorinin bos formu kurulur; onceki alanlar sifirlanir.
                onSecim = { onFormDegis(bosForm(it)) },
                modifier = Modifier.fillMaxWidth()
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
                onClick = onKaydet,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kaydet")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KayitEkleIcerikPreview() {
    MotorumTheme {
        KayitEkleIcerik(
            form = KayitFormu.Yakit(),
            onFormDegis = {},
            onKaydet = {},
            onGeri = {}
        )
    }
}
