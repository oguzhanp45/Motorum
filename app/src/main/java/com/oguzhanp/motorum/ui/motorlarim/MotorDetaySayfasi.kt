package com.oguzhanp.motorum.ui.motorlarim

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.core.constants.AppSpacing
import com.oguzhanp.motorum.ui.components.EtiketliAlan
import com.oguzhanp.motorum.ui.components.MurekkepDugme
import com.oguzhanp.motorum.ui.theme.MetinIkincil
import com.oguzhanp.motorum.ui.theme.MetinSolgun
import com.oguzhanp.motorum.ui.theme.MotorumTheme
import com.oguzhanp.motorum.ui.theme.SekmeZemin
import com.oguzhanp.motorum.ui.components.MotorumIkonlari

@Composable
fun MotorDetaySayfasi(
    navController: NavController,
    motorId: String?,
    secilsin: Boolean = false,
    viewModel: MotorDetayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // PickVisualMedia calisma zamani izni istemiyor: kullanici sistem
    // seciciden hangi gorseli verdiyse uygulama sadece ona erisiyor.
    val galeriSecici = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let(viewModel::fotografSec) }

    // Kameraya once bos bir dosya veriyoruz, o dosyaya yaziyor. Hangi dosyayi
    // verdigimizi sonucta ogrenemedigimiz icin burada sakliyoruz.
    var kameraHedefi by remember { mutableStateOf<Uri?>(null) }
    val kameraSecici = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { basarili ->
        val hedef = kameraHedefi
        if (basarili && hedef != null) viewModel.fotografCekildi(hedef)
        kameraHedefi = null
    }

    LaunchedEffect(Unit) { viewModel.baslat(motorId, secilsin) }

    LaunchedEffect(uiState.bitti) {
        if (uiState.bitti) navController.popBackStack()
    }

    MotorDetayIcerik(
        uiState = uiState,
        onFormDegis = viewModel::formDegis,
        onKaydetTikla = viewModel::kaydet,
        onGeriTikla = { navController.popBackStack() },
        onGaleriAc = {
            galeriSecici.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onKameraAc = {
            val hedef = viewModel.kameraHedefi()
            if (hedef == null) {
                viewModel.kameraAcilamadi()
            } else {
                kameraHedefi = hedef
                // Kamera uygulamasi olmayan cihazda launch hata firlatiyor.
                try {
                    kameraSecici.launch(hedef)
                } catch (hata: Exception) {
                    kameraHedefi = null
                    viewModel.kameraAcilamadi()
                }
            }
        },
        onFotografKaldir = viewModel::fotografKaldir
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotorDetayIcerik(
    uiState: MotorDetayUiState,
    onFormDegis: (MotorFormu) -> Unit,
    onKaydetTikla: () -> Unit,
    onGeriTikla: () -> Unit,
    onGaleriAc: () -> Unit,
    onKameraAc: () -> Unit,
    onFotografKaldir: () -> Unit
) {
    val form = uiState.form

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (uiState.yeniMi) R.string.motor_ekle else R.string.motoru_duzenle)) },
                navigationIcon = {
                    IconButton(onClick = onGeriTikla) {
                        Icon(MotorumIkonlari.Geri, contentDescription = stringResource(R.string.geri))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { icPadding ->
        Column(
            modifier = Modifier
                .padding(icPadding)
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.normal),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FotografAlani(
                fotograf = uiState.fotograf,
                calisiyor = uiState.fotografCalisiyor,
                onGaleriAc = onGaleriAc,
                onKameraAc = onKameraAc,
                onKaldir = onFotografKaldir
            )

            EtiketliAlan(
                etiket = stringResource(R.string.marka),
                zorunlu = true,
                deger = form.marka,
                onDegis = { onFormDegis(form.copy(marka = it, markaHatali = false)) },
                ipucu = stringResource(R.string.marka_ipucu),
                ikon = MotorumIkonlari.Motor,
                hatali = form.markaHatali,
                hataMetni = stringResource(R.string.marka_zorunlu),
                klavye = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            EtiketliAlan(
                etiket = stringResource(R.string.model),
                zorunlu = true,
                deger = form.model,
                onDegis = { onFormDegis(form.copy(model = it, modelHatali = false)) },
                ipucu = stringResource(R.string.model_ipucu),
                ikon = MotorumIkonlari.Gosterge,
                hatali = form.modelHatali,
                hataMetni = stringResource(R.string.model_zorunlu)
            )

            EtiketliAlan(
                etiket = stringResource(R.string.plaka),
                deger = form.plaka,
                onDegis = { onFormDegis(form.copy(plaka = it)) },
                ipucu = stringResource(R.string.plaka_ipucu),
                ikon = MotorumIkonlari.Plaka,
                // Buyuk harfe cevirmeyi klavyeye birakmiyoruz: klavyenin dili
                // Ingilizce oldugunda "i" harfini "I" yapiyordu, "İ" degil.
                // Yazilan neyse o kaliyor.
                klavye = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            if (uiState.hata != null) {
                Text(
                    text = uiState.hata,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            MurekkepDugme(
                onClick = onKaydetTikla,
                enabled = !uiState.calisiyor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (uiState.calisiyor) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.kaydet))
                }
            }
        }
    }
}

// Motosiklet fotograflari yatay oluyor, o yuzden alan 3:2. Kare olsaydi
// fotografin saginda solunda kalanlari kirpardik.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FotografAlani(
    fotograf: ImageBitmap?,
    calisiyor: Boolean,
    onGaleriAc: () -> Unit,
    onKameraAc: () -> Unit,
    onKaldir: () -> Unit
) {
    // Panelin acik olup olmadigi ekranin kendi isi, disari tasimiyoruz.
    var panelAcik by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 2f)
            .clip(RoundedCornerShape(12.dp))
            .background(SekmeZemin)
            .clickable(enabled = !calisiyor) { panelAcik = true },
        contentAlignment = Alignment.Center
    ) {
        if (fotograf != null) {
            Image(
                bitmap = fotograf,
                contentDescription = stringResource(R.string.motor_fotografi),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    MotorumIkonlari.Fotograf,
                    contentDescription = null,
                    tint = MetinSolgun,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = stringResource(R.string.fotograf_ekle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MetinIkincil
                )
            }
        }

        if (calisiyor) CircularProgressIndicator()
    }

    if (panelAcik) {
        ModalBottomSheet(
            onDismissRequest = { panelAcik = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = stringResource(R.string.motor_fotografi_baslik),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                PanelSatiri(
                    ikon = MotorumIkonlari.Galeri,
                    metin = stringResource(R.string.galeriden_sec),
                    onTikla = {
                        panelAcik = false
                        onGaleriAc()
                    }
                )
                PanelSatiri(
                    ikon = MotorumIkonlari.Fotograf,
                    metin = stringResource(R.string.fotograf_cek),
                    onTikla = {
                        panelAcik = false
                        onKameraAc()
                    }
                )
                if (fotograf != null) {
                    PanelSatiri(
                        ikon = MotorumIkonlari.Sil,
                        metin = stringResource(R.string.kaldir),
                        renk = MaterialTheme.colorScheme.error,
                        onTikla = {
                            panelAcik = false
                            onKaldir()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PanelSatiri(
    ikon: ImageVector,
    metin: String,
    onTikla: () -> Unit,
    renk: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onTikla)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(ikon, contentDescription = null, tint = renk, modifier = Modifier.size(22.dp))
        Text(text = metin, style = MaterialTheme.typography.bodyLarge, color = renk)
    }
}

@Preview(showBackground = true)
@Composable
private fun MotorDetayIcerikPreview() {
    MotorumTheme {
        MotorDetayIcerik(
            uiState = MotorDetayUiState(),
            onFormDegis = {},
            onKaydetTikla = {},
            onGeriTikla = {},
            onGaleriAc = {},
            onKameraAc = {},
            onFotografKaldir = {}
        )
    }
}
