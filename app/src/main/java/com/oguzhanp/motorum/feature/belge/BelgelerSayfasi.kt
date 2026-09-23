package com.oguzhanp.motorum.feature.belge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.core.tasarim.AppSpacing
import com.oguzhanp.motorum.model.Belge
import com.oguzhanp.motorum.model.BelgeTuru
import com.oguzhanp.motorum.core.tasarim.MotorumIkonlari
import com.oguzhanp.motorum.core.navigation.Routes
import com.oguzhanp.motorum.core.tasarim.MetinIkincil
import com.oguzhanp.motorum.core.tasarim.MotorumTheme

// Ayarlar'dan acilan Belgeler sayfasi. Istatistikler'deki kartin aynisini
// tek basina gosteriyor; iki yerde ayni bilesen, ayni davranis.
@Composable
fun BelgelerSayfasi(
    navController: NavController,
    viewModel: BelgelerViewModel = hiltViewModel()
) {
    // Belge sayfasindan donunce liste tazelensin.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.yukle() }
    val durum by viewModel.uiState.collectAsStateWithLifecycle()

    BelgelerIcerik(
        durum = durum,
        onGeri = { navController.popBackStack() },
        onEkle = { navController.navigate(Routes.belgeRotasi()) },
        onBelgeTikla = { navController.navigate(Routes.belgeRotasi(id = it.id)) },
        onBosTurTikla = { navController.navigate(Routes.belgeRotasi(tur = it.name)) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BelgelerIcerik(
    durum: BelgelerUiState,
    onGeri: () -> Unit,
    onEkle: () -> Unit,
    onBelgeTikla: (Belge) -> Unit,
    onBosTurTikla: (BelgeTuru) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.belgeler)) },
                navigationIcon = {
                    IconButton(onClick = onGeri) {
                        Icon(MotorumIkonlari.Geri, contentDescription = stringResource(R.string.geri))
                    }
                }
            )
        }
    ) { icPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(icPadding)
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.normal),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.orta)
        ) {
            Text(
                text = stringResource(R.string.belgeler_sayfa_aciklama),
                style = MaterialTheme.typography.bodySmall,
                color = MetinIkincil
            )
            BelgelerKarti(
                durum = durum,
                onEkle = onEkle,
                onBelgeTikla = onBelgeTikla,
                onBosTurTikla = onBosTurTikla
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BelgelerIcerikPreview() {
    MotorumTheme {
        BelgelerIcerik(
            durum = BelgelerUiState(yukleniyor = false),
            onGeri = {}, onEkle = {}, onBelgeTikla = {}, onBosTurTikla = {}
        )
    }
}
