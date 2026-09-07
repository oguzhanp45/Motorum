package com.oguzhanp.motorum.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.oguzhanp.motorum.model.Kategori
import com.oguzhanp.motorum.ui.theme.AksesuarMetin
import com.oguzhanp.motorum.ui.theme.AksesuarRenk
import com.oguzhanp.motorum.ui.theme.AksesuarZemin
import com.oguzhanp.motorum.ui.theme.BakimMetin
import com.oguzhanp.motorum.ui.theme.BakimRenk
import com.oguzhanp.motorum.ui.theme.BakimZemin
import com.oguzhanp.motorum.ui.theme.RoadTripMetin
import com.oguzhanp.motorum.ui.theme.RoadTripRenk
import com.oguzhanp.motorum.ui.theme.RoadTripZemin
import com.oguzhanp.motorum.ui.theme.YakitMetin
import com.oguzhanp.motorum.ui.theme.YakitRenk
import com.oguzhanp.motorum.ui.theme.YakitZemin

// Kategoriye ait gorsel bilgiler burada, model katmaninda degil:
// Kayit ve Kategori Compose'u tanimamali, yarin veritabanina yazilirken
// ikon ve renk orada isi olmaz.
// Yeni kategori eklendiginde derleyici bu when'i de gosterir.
data class KategoriGorunumu(
    val ikon: ImageVector,
    val renk: Color,
    val zemin: Color,
    val metin: Color,
    val rozet: String,
    val notEtiketi: String
)

fun gorunum(kategori: Kategori): KategoriGorunumu = when (kategori) {
    Kategori.YAKIT -> KategoriGorunumu(
        ikon = Icons.Default.LocalGasStation,
        renk = YakitRenk,
        zemin = YakitZemin,
        metin = YakitMetin,
        rozet = "DOLUM",
        notEtiketi = "İstasyon (isteğe bağlı)"
    )

    Kategori.ROAD_TRIP -> KategoriGorunumu(
        ikon = Icons.Default.Map,
        renk = RoadTripRenk,
        zemin = RoadTripZemin,
        metin = RoadTripMetin,
        rozet = "SÜRÜŞ",
        notEtiketi = "Not (isteğe bağlı)"
    )

    Kategori.BAKIM -> KategoriGorunumu(
        ikon = Icons.Default.Build,
        renk = BakimRenk,
        zemin = BakimZemin,
        metin = BakimMetin,
        rozet = "SERVİS",
        notEtiketi = "Not (isteğe bağlı)"
    )

    Kategori.AKSESUAR -> KategoriGorunumu(
        ikon = Icons.Default.Shield,
        renk = AksesuarRenk,
        zemin = AksesuarZemin,
        metin = AksesuarMetin,
        rozet = "EKİPMAN",
        notEtiketi = "Dükkan (isteğe bağlı)"
    )
}
