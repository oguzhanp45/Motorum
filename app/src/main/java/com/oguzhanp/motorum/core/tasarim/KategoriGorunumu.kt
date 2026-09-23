package com.oguzhanp.motorum.core.tasarim

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.model.Kategori
import com.oguzhanp.motorum.core.tasarim.MotorumIkonlari
import com.oguzhanp.motorum.core.tasarim.AksesuarMetin
import com.oguzhanp.motorum.core.tasarim.AksesuarRenk
import com.oguzhanp.motorum.core.tasarim.AksesuarZemin
import com.oguzhanp.motorum.core.tasarim.BakimMetin
import com.oguzhanp.motorum.core.tasarim.BakimRenk
import com.oguzhanp.motorum.core.tasarim.BakimZemin
import com.oguzhanp.motorum.core.tasarim.RoadTripMetin
import com.oguzhanp.motorum.core.tasarim.RoadTripRenk
import com.oguzhanp.motorum.core.tasarim.RoadTripZemin
import com.oguzhanp.motorum.core.tasarim.YakitMetin
import com.oguzhanp.motorum.core.tasarim.YakitRenk
import com.oguzhanp.motorum.core.tasarim.YakitZemin

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
    // Not alaninin etiketi kategoriye gore degisiyor; metin degil kimligi.
    @StringRes val notEtiketi: Int
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
        notEtiketi = R.string.not_istasyon
    )

    Kategori.ROAD_TRIP -> KategoriGorunumu(
        ikon = MotorumIkonlari.RoadTrip,
        renk = RoadTripRenk,
        zemin = RoadTripZemin,
        metin = RoadTripMetin,
        notEtiketi = R.string.not_serbest
    )

    Kategori.BAKIM -> KategoriGorunumu(
        ikon = MotorumIkonlari.Bakim,
        renk = BakimRenk,
        zemin = BakimZemin,
        metin = BakimMetin,
        notEtiketi = R.string.not_serbest
    )

    Kategori.AKSESUAR -> KategoriGorunumu(
        ikon = MotorumIkonlari.Aksesuar,
        renk = AksesuarRenk,
        zemin = AksesuarZemin,
        metin = AksesuarMetin,
        notEtiketi = R.string.not_dukkan
    )
}
