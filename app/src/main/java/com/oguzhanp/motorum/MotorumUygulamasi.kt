package com.oguzhanp.motorum

import android.app.Application
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.oguzhanp.motorum.data.hatirlatma.BELGE_KANALI
import com.oguzhanp.motorum.data.hatirlatma.BILDIRIM_KANALI
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
            .setName(getString(R.string.bakim_hatirlatmalari))
            .setDescription(getString(R.string.kanal_bakim_aciklama))
            .build()

        val belgeKanali = NotificationChannelCompat
            .Builder(BELGE_KANALI, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(getString(R.string.kanal_belge))
            .setDescription(getString(R.string.kanal_belge_aciklama))
            .build()

        NotificationManagerCompat.from(this).createNotificationChannelsCompat(listOf(kanal, belgeKanali))
    }
}
