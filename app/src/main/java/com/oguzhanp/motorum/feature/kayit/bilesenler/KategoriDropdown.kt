package com.oguzhanp.motorum.feature.kayit.bilesenler

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oguzhanp.motorum.core.tasarim.AppMotion
import com.oguzhanp.motorum.core.tasarim.CizgiSolgun
import com.oguzhanp.motorum.core.tasarim.Inter
import com.oguzhanp.motorum.core.tasarim.KartZemin
import com.oguzhanp.motorum.core.tasarim.MetinAna
import com.oguzhanp.motorum.core.tasarim.MetinIkincil
import com.oguzhanp.motorum.core.tasarim.MotorumIkonlari
import com.oguzhanp.motorum.core.tasarim.MotorumTheme
import com.oguzhanp.motorum.core.tasarim.gorunum
import com.oguzhanp.motorum.model.Kategori

// Kategori secimi icin acilir menu (tasarim: Kategori Menusu).
// Kendi ic durumu: menu acik mi (acik). Disariyi ilgilendirmedigi icin burada tutuluyor.
// Secim ise disari bildiriliyor: onSecim(...)  -> "state asagi, olay yukari"
// Yapi Material'in ExposedDropdownMenuBox'i; tasarimdaki gibi satirlara kategori
// ikonu ve rengi eklendi, secili olan tikle isaretli.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KategoriDropdown(
    secili: Kategori,
    onSecim: (Kategori) -> Unit,
    modifier: Modifier = Modifier
) {
    var acik by remember { mutableStateOf(false) }
    // Ok acilinca yukari donuyor.
    val okAcisi by animateFloatAsState(
        targetValue = if (acik) 180f else 0f,
        animationSpec = tween(AppMotion.PANEL, easing = AppMotion.egri),
        label = "kategoriOku"
    )

    ExposedDropdownMenuBox(
        expanded = acik,
        onExpandedChange = { acik = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = stringResource(secili.ad),
            onValueChange = { },
            readOnly = true,
            label = { Text("Kategori") },
            trailingIcon = {
                Icon(
                    MotorumIkonlari.AsagiOk,
                    contentDescription = null,
                    tint = MetinIkincil,
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer { rotationZ = okAcisi }
                )
            },
            // menuAnchor: menu bu kutunun altinda acilsin demek
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = acik,
            onDismissRequest = { acik = false },
            shape = RoundedCornerShape(14.dp),
            containerColor = KartZemin
        ) {
            // Kategori.entries: enum'daki tum degerler.
            // Yeni kategori eklersen bu dosyaya dokunmadan menude gorunur.
            Kategori.entries.forEach { kategori ->
                val gorunum = gorunum(kategori)
                val seciliMi = kategori == secili
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(kategori.ad),
                            style = TextStyle(
                                fontFamily = Inter,
                                fontSize = 14.5.sp,
                                fontWeight = if (seciliMi) FontWeight.Bold else FontWeight.SemiBold
                            ),
                            color = MetinAna
                        )
                    },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(gorunum.zemin),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(gorunum.ikon, contentDescription = null, tint = gorunum.renk, modifier = Modifier.size(17.dp))
                        }
                    },
                    trailingIcon = if (seciliMi) {
                        { Icon(MotorumIkonlari.Onay, contentDescription = "Seçili", tint = MetinAna, modifier = Modifier.size(18.dp)) }
                    } else null,
                    // Secili satir hafif gri zeminde: menu acildiginda goz oraya gitsin.
                    modifier = if (seciliMi) Modifier.background(CizgiSolgun) else Modifier,
                    onClick = {
                        onSecim(kategori)
                        acik = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KategoriDropdownPreview() {
    MotorumTheme {
        KategoriDropdown(
            secili = Kategori.YAKIT,
            onSecim = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
