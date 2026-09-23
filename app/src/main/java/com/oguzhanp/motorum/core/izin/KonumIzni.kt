package com.oguzhanp.motorum.core.izin

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.oguzhanp.motorum.R

// Ikisi birlikte isteniyor. FINE tek basina gonderilirse Android 12'den beri
// sistem istegi yok sayiyor, yani izin diyalogu hic cikmiyor.
private val KONUM_IZINLERI = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

// "Yaklasik" secilirse sadece COARSE veriliyor ve o da bize yetiyor.
// Bu yuzden "FINE var mi" degil, "ikisinden biri var mi" diye soruyoruz.
fun konumIzniVerildiMi(baglam: Context): Boolean =
    KONUM_IZINLERI.any {
        ContextCompat.checkSelfPermission(baglam, it) == PackageManager.PERMISSION_GRANTED
    }

// Bildirim izniyle ayni kalip: donen fonksiyon cagrilinca akis basliyor,
// sonuc onSonuc ile geliyor. Sadece stateful ekranlarda cagrilabilir,
// @Preview icinde calismaz.
@Composable
fun rememberKonumIzni(onSonuc: (verildi: Boolean) -> Unit): () -> Unit {
    val baglam = LocalActivity.current
    var aciklamaAcik by remember { mutableStateOf(false) }

    // Coklu istekte sonuc harita donuyor; biri bile verildiyse yeter.
    val isteyici = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { sonuclar -> onSonuc(sonuclar.values.any { it }) }

    if (aciklamaAcik) {
        AlertDialog(
            onDismissRequest = {
                aciklamaAcik = false
                onSonuc(false)
            },
            title = { Text(stringResource(R.string.konum_izni)) },
            text = {
                Text(
                    stringResource(R.string.konum_izni_metin)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    aciklamaAcik = false
                    isteyici.launch(KONUM_IZINLERI)
                }) { Text(stringResource(R.string.devam)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    aciklamaAcik = false
                    onSonuc(false)
                }) { Text(stringResource(R.string.vazgec)) }
            }
        )
    }

    return {
        val etkinlik = baglam
        when {
            etkinlik == null -> onSonuc(false)

            konumIzniVerildiMi(etkinlik) -> onSonuc(true)

            // Kullanici daha once reddetmis ama kapiyi kapatmamis: once
            // kendi aciklamamizi gosteriyoruz.
            KONUM_IZINLERI.any {
                ActivityCompat.shouldShowRequestPermissionRationale(etkinlik, it)
            } -> aciklamaAcik = true

            // Ya hic sorulmadi ya da kalici reddedildi. Ayirt etmenin yolu
            // yok ama yapacagimiz sey ayni: sistemden iste.
            else -> isteyici.launch(KONUM_IZINLERI)
        }
    }
}
