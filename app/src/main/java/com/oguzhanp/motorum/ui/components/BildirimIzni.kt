package com.oguzhanp.motorum.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

// Android 12 ve altinda boyle bir izin yok, bildirim dogrudan gosteriliyor.
fun bildirimIzniVerildiMi(baglam: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                baglam,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

// Bildirim iznini isteyen kapi. Donen fonksiyon cagrildiginda akis basliyor,
// sonuc onSonuc ile bildiriliyor.
//
// Bu bilesen stateful ekranlarda cagriliyor, icerik bilesenlerinde degil:
// rememberLauncherForActivityResult'in bir Activity kaydina ihtiyaci var ve
// @Preview icinde boyle bir kayit yok. Kamera ve galeri seciciyle ayni kalip.
@Composable
fun rememberBildirimIzni(onSonuc: (verildi: Boolean) -> Unit): () -> Unit {
    val baglam = LocalActivity.current
    var aciklamaAcik by remember { mutableStateOf(false) }

    val isteyici = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { verildi -> onSonuc(verildi) }

    // Kullanici daha once reddetmis ama kapiyi kapatmamis. Android'in istedigi
    // sey tam olarak bu: sistem diyalogunu tekrar acmadan once neden gerektigini
    // kendi dilimizle anlatiyoruz.
    if (aciklamaAcik) {
        AlertDialog(
            onDismissRequest = {
                aciklamaAcik = false
                onSonuc(false)
            },
            title = { Text("Bildirim izni") },
            text = {
                Text(
                    "Bakım zamanı geldiğinde sana haber verebilmemiz için " +
                            "bildirim izni gerekiyor."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    aciklamaAcik = false
                    isteyici.launch(Manifest.permission.POST_NOTIFICATIONS)
                }) { Text("Devam") }
            },
            dismissButton = {
                TextButton(onClick = {
                    aciklamaAcik = false
                    onSonuc(false)
                }) { Text("Vazgeç") }
            }
        )
    }

    return {
        val etkinlik = baglam
        when {
            etkinlik == null -> onSonuc(false)

            bildirimIzniVerildiMi(etkinlik) -> onSonuc(true)

            ActivityCompat.shouldShowRequestPermissionRationale(
                etkinlik,
                Manifest.permission.POST_NOTIFICATIONS
            ) -> aciklamaAcik = true

            // Ya hic sorulmadi ya da kalici olarak reddedildi. shouldShow...
            // ikisinde de false donuyor ve ayirt etmenin baska yolu yok.
            // Ama yapacagimiz sey ayni: sistemden izni iste. Hic sorulmadiysa
            // diyalog cikar; kalici ret varsa cikmaz ve geri cagri hemen
            // false doner. Iki durumda da sonuc ayni yerden geciyor.
            else -> isteyici.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
