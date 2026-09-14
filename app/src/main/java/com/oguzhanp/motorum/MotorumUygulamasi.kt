package com.oguzhanp.motorum

import android.app.Application
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.oguzhanp.motorum.data.BILDIRIM_KANALI
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MotorumUygulamasi : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseFirestore.getInstance().firestoreSettings =
            FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                .build()

        bildirimKanaliniKur()
    }

    // Android 8'den beri kanalsiz bildirim hic gorunmuyor. Kanali burada
    // kuruyoruz cunku bildirimin gosterilecegi an uygulama kapali olabilir;
    // alici uyandiginda kanal coktan hazir olmali.
    // Ayni kimlikle tekrar kurmak zararsiz, sistem mevcut kanali koruyor.
    private fun bildirimKanaliniKur() {
        val kanal = NotificationChannelCompat
            .Builder(BILDIRIM_KANALI, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName("Bakım hatırlatmaları")
            .setDescription("Planladığın bakımların zamanı geldiğinde bildirir.")
            .build()

        NotificationManagerCompat.from(this).createNotificationChannel(kanal)
    }
}
