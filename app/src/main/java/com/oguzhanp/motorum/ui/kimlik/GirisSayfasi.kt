package com.oguzhanp.motorum.ui.kimlik

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oguzhanp.motorum.core.constants.AppSpacing
import com.oguzhanp.motorum.ui.theme.MotorumTheme

@Composable
fun GirisSayfasi(
    viewModel: GirisViewModel,
    onUyeOlaGit: () -> Unit,
    onGirisBasarili: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.basarili) {
        if (uiState.basarili) {
            onGirisBasarili()
            viewModel.basariliTuketildi()
        }
    }

    GirisIcerik(
        uiState = uiState,
        onFormDegis = viewModel::formDegis,
        onGirisTikla = viewModel::girisYap,
        onUyeOlaGit = onUyeOlaGit
    )
}

@Composable
fun GirisIcerik(
    uiState: GirisUiState,
    onFormDegis: (KimlikFormu) -> Unit,
    onGirisTikla: () -> Unit,
    onUyeOlaGit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.normal, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        KimlikBasligi()

        KimlikSekmesi(
            girisSecili = true,
            onGirisTikla = {},
            onUyeOlTikla = onUyeOlaGit
        )

        KimlikKarti(
            baslik = "Hesabınıza Giriş Yapın",
            butonMetni = "Giriş Yap",
            form = uiState.form,
            yukleniyor = uiState.yukleniyor,
            hata = uiState.hata,
            onFormDegis = onFormDegis,
            onGonderTikla = onGirisTikla
        )

        KimlikAltBaglantisi(
            soru = "Henüz bir hesabınız yok mu?",
            baglanti = "Kayıt Ol",
            onTikla = onUyeOlaGit
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GirisIcerikPreview() {
    MotorumTheme {
        GirisIcerik(
            uiState = GirisUiState(),
            onFormDegis = {},
            onGirisTikla = {},
            onUyeOlaGit = {}
        )
    }
}
