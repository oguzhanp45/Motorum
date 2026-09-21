package com.oguzhanp.motorum.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.oguzhanp.motorum.model.Kategori
import com.oguzhanp.motorum.ui.components.MotorumIkonlari
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

// Ikonlar MotorumIkonlari'ndan geliyor: maketlerdeki cizimlerin birebir
// karsiligi. Material'in hazir ikonlari dolu govdeliydi, bizim tasarimimiz
// cizgi tabanli; ikisini karistirinca ekran iki ayri setten derlenmis duruyordu.
// Kategoriye ait gorsel bilgiler burada, model katmaninda degil:
// Kayit ve Kategori Compose'u tanimamali, yarin veritabanina yazilirken
// ikon ve renk orada isi olmaz.
// Yeni kategori eklendiginde derleyici bu when'i de gosterir.
data class KategoriGorunumu(
    val ikon: ImageVector,
    val renk: Color,
    val zemin: Color,
    val metin: Color,
    val notEtiketi: String
)

// @Composable cunku renkler temadan okunuyor: ayni kategori acik temada
// acik, karanlikta koyu zeminle geliyor.
@Composable
@ReadOnlyComposable
fun gorunum(kategori: Kategori): KategoriGorunumu = when (kategori) {
    Kategori.YAKIT -> KategoriGorunumu(
        ikon = MotorumIkonlari.Yakit,
        renk = YakitRenk,
        zemin = YakitZemin,
        metin = YakitMetin,
        notEtiketi = "İstasyon (isteğe bağlı)"
    )

    Kategori.ROAD_TRIP -> KategoriGorunumu(
        ikon = MotorumIkonlari.RoadTrip,
        renk = RoadTripRenk,
        zemin = RoadTripZemin,
        metin = RoadTripMetin,
        notEtiketi = "Not (isteğe bağlı)"
    )

    Kategori.BAKIM -> KategoriGorunumu(
        ikon = MotorumIkonlari.Bakim,
        renk = BakimRenk,
        zemin = BakimZemin,
        metin = BakimMetin,
        notEtiketi = "Not (isteğe bağlı)"
    )

    Kategori.AKSESUAR -> KategoriGorunumu(
        ikon = MotorumIkonlari.Aksesuar,
        renk = AksesuarRenk,
        zemin = AksesuarZemin,
        metin = AksesuarMetin,
        notEtiketi = "Dükkan (isteğe bağlı)"
    )
}
