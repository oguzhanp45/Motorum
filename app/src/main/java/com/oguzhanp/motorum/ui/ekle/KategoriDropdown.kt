package com.oguzhanp.motorum.ui.ekle

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.oguzhanp.motorum.model.Kategori

// Kategori secimi icin acilir menu.
// Kendi ic durumu: menu acik mi (acik). Disariyi ilgilendirmedigi icin burada tutuluyor.
// Secim ise disari bildiriliyor: onSecim(...)  -> "state asagi, olay yukari"
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KategoriDropdown(
    secili: Kategori,
    onSecim: (Kategori) -> Unit,
    modifier: Modifier = Modifier
) {
    var acik by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = acik,
        onExpandedChange = { acik = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = secili.etiket,
            onValueChange = { },
            readOnly = true,
            label = { Text("Kategori") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = acik) },
            // menuAnchor: menu bu kutunun altinda acilsin demek
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = acik,
            onDismissRequest = { acik = false }
        ) {
            // Kategori.entries: enum'daki tum degerler.
            // Yeni kategori eklersen bu dosyaya dokunmadan menude gorunur.
            Kategori.entries.forEach { kategori ->
                DropdownMenuItem(
                    text = { Text(kategori.etiket) },
                    onClick = {
                        onSecim(kategori)
                        acik = false
                    }
                )
            }
        }
    }
}
