package com.oguzhanp.motorum.ui.ekle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.core.constants.AppSpacing
import com.oguzhanp.motorum.model.Kayit
import com.oguzhanp.motorum.ui.ekle.components.KategoriDropdown
import com.oguzhanp.motorum.ui.ekle.components.TarihSecici
import com.oguzhanp.motorum.ui.home.KayitViewModel
import com.oguzhanp.motorum.ui.theme.MotorumTheme

// Tek hata mesaji: hem bos birakilan hem de sayiya cevrilemeyen girdi icin.
private const val ALAN_HATA_MESAJI = "Zorunlu alan — geçerli bir sayı gir kRAL"


@Composable
fun KayitEkleSayfasi(
    kayitViewModel: KayitViewModel,                     // paylasilan liste
    navController: NavController,
    ekleViewModel: KayitEkleViewModel = viewModel()     // bu ekrana ozel form
) {
    val form by ekleViewModel.uiState.collectAsStateWithLifecycle()

    KayitEkleIcerik(
        form = form,
        onFormDegis = ekleViewModel::guncelle,
        onGeri = { navController.popBackStack() },
        onKaydet = {
            // dogrula() hata bayraklari isaretlenmis YENI bir state dondurur.
            val kontrol = form.dogrula()
            if (kontrol.gecerli) {
                kayitViewModel.ekle(
                    Kayit(
                        kategori = kontrol.kategori,
                        tarihMillis = kontrol.tarihMillis,
                        // gecerli == true oldugu icin bunlar null olamaz
                        litre = kontrol.litre!!,
                        tutar = kontrol.tutar!!,
                        not = kontrol.not.trim()
                    )
                )
                navController.popBackStack()
            } else {
                // Hatali state'i yaz -> kirmizi uyarilar gorunur
                ekleViewModel.guncelle(kontrol)
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KayitEkleIcerik(
    form: KayitEkleUiState,
    onFormDegis: (KayitEkleUiState) -> Unit,
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
                .padding(AppSpacing.normal),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KategoriDropdown(
                secili = form.kategori,
                onSecim = { onFormDegis(form.copy(kategori = it)) },
                modifier = Modifier.fillMaxWidth()
            )

            TarihSecici(
                tarihMillis = form.tarihMillis,
                onTarihSec = { onFormDegis(form.copy(tarihMillis = it)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = form.litreYazi,
                onValueChange = { onFormDegis(form.copy(litreYazi = it, litreHatali = false)) },
                label = { Text("Litre") },
                isError = form.litreHatali,
                supportingText = {
                    if (form.litreHatali) Text(ALAN_HATA_MESAJI)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = form.tutarYazi,
                onValueChange = { onFormDegis(form.copy(tutarYazi = it, tutarHatali = false)) },
                label = { Text("Tutar (₺)") },
                isError = form.tutarHatali,
                supportingText = {
                    if (form.tutarHatali) Text(ALAN_HATA_MESAJI)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = form.not,
                onValueChange = { onFormDegis(form.copy(not = it)) },
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
            form = KayitEkleUiState(),
            onFormDegis = {},
            onKaydet = {},
            onGeri = {}
        )
    }
}
