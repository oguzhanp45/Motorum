package com.oguzhanp.motorum.ui.motorlarim

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import com.oguzhanp.motorum.model.Motor
import com.oguzhanp.motorum.ui.theme.MotorumTheme
import com.oguzhanp.motorum.ui.theme.MotorArduvaz
import com.oguzhanp.motorum.ui.theme.MotorArduvazZemin
import com.oguzhanp.motorum.ui.theme.MotorCamgobegi
import com.oguzhanp.motorum.ui.theme.MotorCamgobegiZemin
import com.oguzhanp.motorum.ui.theme.MotorGok
import com.oguzhanp.motorum.ui.theme.MotorGokZemin
import com.oguzhanp.motorum.ui.theme.MotorIndigo
import com.oguzhanp.motorum.ui.theme.MotorIndigoZemin
import com.oguzhanp.motorum.ui.theme.MotorLacivert
import com.oguzhanp.motorum.ui.theme.MotorLacivertZemin
import com.oguzhanp.motorum.ui.components.MotorumIkonlari

data class MotorGorunumu(val renk: Color, val zemin: Color)

// Palet temaya gore degisiyor, o yuzden sabit bir liste degil: her okumada o
// an gecerli temanin renkleriyle kuruluyor. Sira ayni kaldigi icin bir motorun
// "rengi" iki temada da ayni aileden: acikta indigo, karanlikta acik indigo.
@Composable
@ReadOnlyComposable
private fun motorPaleti(): List<MotorGorunumu> = listOf(
    MotorGorunumu(MotorIndigo, MotorIndigoZemin),
    MotorGorunumu(MotorGok, MotorGokZemin),
    MotorGorunumu(MotorLacivert, MotorLacivertZemin),
    MotorGorunumu(MotorArduvaz, MotorArduvazZemin),
    MotorGorunumu(MotorCamgobegi, MotorCamgobegiZemin)
)

// Renk motorun kimliginden turuyor, listedeki sirasindan degil: sirayla
// dagitsaydik bir motor silinince kalanlarin rengi kayardi.
// String.hashCode() Java'da tanimli ve sabit bir algoritma, yani ayni kimlik
// her cihazda ve her calistirmada ayni rengi veriyor.
// mod (rem degil) negatif hash'te de pozitif sonuc donuyor.
@Composable
@ReadOnlyComposable
fun motorGorunumu(motorId: String): MotorGorunumu {
    val palet = motorPaleti()
    return palet[motorId.hashCode().mod(palet.size)]
}

// base64 metni goruntuye cevirir. Bozuk ya da bos metinde null donuyor:
// ekranda fotograf yerine ikon cikiyor, uygulama cokmuyor.
internal fun base64Coz(base64: String): ImageBitmap? {
    if (base64.isBlank()) return null
    return try {
        val baytlar = Base64.decode(base64, Base64.NO_WRAP)
        BitmapFactory.decodeByteArray(baytlar, 0, baytlar.size)?.asImageBitmap()
    } catch (hata: Exception) {
        null
    }
}

// Motorun kucuk gorseli: fotografi varsa fotograf, yoksa renkli ikon karesi.
// Ayni sey hem motor kartinda hem secim panelinde lazim oldugu icin tek yerde.
@Composable
fun MotorGorseli(
    motor: Motor,
    boyut: Dp,
    modifier: Modifier = Modifier
) {
    val gorunum = motorGorunumu(motor.id)

    // Anahtar onizleme metninin kendisi: metin degismedikce cozme bir kez
    // yapiliyor. Anahtarsiz yazsaydik listeyi her kaydirista tekrar cozulurdu.
    val gorsel = remember(motor.onizleme) { base64Coz(motor.onizleme) }

    Box(
        modifier = modifier
            .size(boyut)
            .clip(RoundedCornerShape(boyut / 4))
            .background(if (gorsel == null) gorunum.zemin else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (gorsel == null) {
            Icon(
                MotorumIkonlari.Motor,
                contentDescription = null,
                tint = gorunum.renk,
                modifier = Modifier.size(boyut * 0.55f)
            )
        } else {
            Image(
                bitmap = gorsel,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// Fotografsiz motorlar: her biri kendi kimliginden turetilen renkte.
// Paletin bes rengini yan yana gormek icin bes farkli kimlik veriliyor.
@Preview(showBackground = true)
@Composable
private fun MotorGorseliPreview() {
    MotorumTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf("a", "b", "c", "d", "e").forEach { kimlik ->
                MotorGorseli(motor = Motor(id = kimlik), boyut = 48.dp)
            }
        }
    }
}
