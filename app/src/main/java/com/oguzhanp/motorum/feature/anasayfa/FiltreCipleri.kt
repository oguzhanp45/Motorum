package com.oguzhanp.motorum.feature.anasayfa

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.core.tasarim.AppShape
import com.oguzhanp.motorum.core.tasarim.AppSpacing
import com.oguzhanp.motorum.core.tasarim.CizgiSolgun
import com.oguzhanp.motorum.core.tasarim.MetinIkincil
import com.oguzhanp.motorum.core.tasarim.MotorumTheme
import com.oguzhanp.motorum.core.tasarim.gorunum
import com.oguzhanp.motorum.model.Kategori

// Listeyi kategoriye gore daraltan cipler. null = "Tumu".
//
// Cipler kategorinin kendi adini kullaniyor, rozet degil: rozet ("DOLUM", "SERVIS")
// kaydin turunu anlatan bir sozcuk, filtre ise kategorinin kendi adi olmali.
//
// LazyRow cunku bes cip dar telefonlarda satira sigmiyor; maketteki dort cipin
// sebebi de buydu, Aksesuar orada yer bulamamisti. Kaydirilabilir satirda
// hepsi duruyor.
@Composable
fun FiltreCipleri(
    secili: Kategori?,
    onSec: (Kategori?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Basa "Tumu" icin null, sonra kategoriler.
    val secenekler = listOf<Kategori?>(null) + Kategori.entries

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.kucuk)
    ) {
        items(secenekler) { kategori ->
            Cip(
                metin = kategori?.let { stringResource(it.ad) } ?: stringResource(R.string.filtre_tumu),
                seciliMi = kategori == secili,
                // Secili olmayan kategori cipi kendi pastel rengiyle duruyor,
                // boylece renk kodu filtreye bakmadan da okunuyor.
                zemin = if (kategori == null) CizgiSolgun else gorunum(kategori).zemin,
                metinRengi = if (kategori == null) MetinIkincil else gorunum(kategori).metin,
                onTikla = { onSec(kategori) }
            )
        }
    }
}

@Composable
private fun Cip(
    metin: String,
    seciliMi: Boolean,
    zemin: Color,
    metinRengi: Color,
    onTikla: () -> Unit
) {
    Text(
        text = metin,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
        // Secili cip murekkep dolgulu: hangi filtrenin acik oldugu tek bakista
        // belli oluyor, renk tonlari arasinda kaybolmuyor.
        color = if (seciliMi) MaterialTheme.colorScheme.surface else metinRengi,
        modifier = Modifier
            .clip(AppShape.cip)
            .background(if (seciliMi) MaterialTheme.colorScheme.onSurface else zemin)
            .clickable(onClick = onTikla)
            .padding(horizontal = 13.dp, vertical = 7.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun FiltreCipleriPreview() {
    MotorumTheme {
        FiltreCipleri(
            secili = null,
            onSec = {},
            modifier = Modifier.padding(vertical = AppSpacing.kucuk)
        )
    }
}
