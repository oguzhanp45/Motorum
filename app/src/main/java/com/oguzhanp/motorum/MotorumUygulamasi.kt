package com.oguzhanp.motorum

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MotorumUygulamasi : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseFirestore.getInstance().firestoreSettings =
            FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                .build()
    }
}
