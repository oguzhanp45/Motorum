package com.oguzhanp.motorum.feature.kimlik

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oguzhanp.motorum.core.tasarim.MotorumTheme

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
        onSifremiUnuttum = viewModel::sifreSifirla,
        onUyeOlaGit = onUyeOlaGit
    )
}

@Composable
fun GirisIcerik(
    uiState: GirisUiState,
    onFormDegis: (KimlikFormu) -> Unit,
    onGirisTikla: () -> Unit,
    onSifremiUnuttum: () -> Unit,
    onUyeOlaGit: () -> Unit
) {
    KimlikEkrani(
        girisSecili = true,
        form = uiState.form,
        yukleniyor = uiState.yukleniyor,
        hata = uiState.hata,
        bilgi = uiState.bilgi,
        onFormDegis = onFormDegis,
        onGonderTikla = onGirisTikla,
        onDigerEkranaGec = onUyeOlaGit,
        onSifremiUnuttum = onSifremiUnuttum
    )
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun GirisIcerikPreview() {
    MotorumTheme(karanlik = false) {
        GirisIcerik(
            uiState = GirisUiState(),
            onFormDegis = {},
            onGirisTikla = {},
            onSifremiUnuttum = {},
            onUyeOlaGit = {}
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun GirisIcerikKaranlikPreview() {
    MotorumTheme(karanlik = true) {
        GirisIcerik(
            uiState = GirisUiState(),
            onFormDegis = {},
            onGirisTikla = {},
            onSifremiUnuttum = {},
            onUyeOlaGit = {}
        )
    }
}
