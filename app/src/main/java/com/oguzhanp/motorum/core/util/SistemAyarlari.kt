package com.oguzhanp.motorum.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

// Telefonun ayarlar ekranina giden iki kapi. Izin kalici olarak reddedilince
// uygulama sistem diyalogunu bir daha acamiyor; kullaniciyi ancak buraya
// yonlendirebiliyoruz. Hava karti da ayarlar sayfasi da ayni kapiyi kullaniyor.

// Uygulamanin bilgi sayfasi: izinler, depolama, bildirimler hepsi burada.
fun uygulamaAyarlariniAc(baglam: Context) {
    baglam.startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", baglam.packageName, null)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

// Dogrudan uygulamanin bildirim ayarlari (minSdk 29, bu sayfa her surumde var).
fun bildirimAyarlariniAc(baglam: Context) {
    baglam.startActivity(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, baglam.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
