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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.core.constants.AppSpacing
import com.oguzhanp.motorum.model.Kategori
import com.oguzhanp.motorum.model.Kayit
import com.oguzhanp.motorum.viewmodel.KayitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KayitEkleSayfasi(
    viewModel: KayitViewModel,
    navController: NavController
) {


    // Form state'i burada tutuluyor cunku gecici:
    // kullanici vazgecip geri donerse cope gitmeli.
    var kategori by remember { mutableStateOf(Kategori.YAKIT) }
    var tarihMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var litreYazi by remember { mutableStateOf("") }
    var tutarYazi by remember { mutableStateOf("") }
    var not by remember { mutableStateOf("") }

    // Metin kutusunun state'i her zaman String'dir; sayiya kaydetme aninda cevrilir.
    val litre = litreYazi.replace(',', '.').toDoubleOrNull()
    val tutar = tutarYazi.replace(',', '.').toDoubleOrNull()
    val kaydedilebilir = litre != null && tutar != null && litre > 0 && tutar > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.kayit_ekle)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                secili = kategori,
                onSecim = { kategori = it },
                modifier = Modifier.fillMaxWidth()
            )

            TarihSecici(
                tarihMillis = tarihMillis,
                onTarihSec = { tarihMillis = it },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = litreYazi,
                onValueChange = { litreYazi = it },
                label = { Text("Litre") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = tutarYazi,
                onValueChange = { tutarYazi = it },
                label = { Text("Tutar (₺)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = not,
                onValueChange = { not = it },
                label = { Text("Not (isteğe bağlı)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    // 1) listeye ekle   2) geri don
                    viewModel.ekle(
                        Kayit(
                            kategori = kategori,
                            tarihMillis = tarihMillis,
                            litre = litre!!,
                            tutar = tutar!!,
                            not = not.trim()
                        )
                    )
                    navController.popBackStack()
                },
                // Gecersiz veri varken buton sonuk ve basilamaz
                enabled = kaydedilebilir,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kaydet")
            }
        }
    }
}
