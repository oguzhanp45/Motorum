package com.oguzhanp.motorum.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// Android telefon yeniden baslayinca butun alarmlari siliyor. Telefon acilinca
// (ve uygulama guncellenince) bu alici uyaniyor ve telefondaki hatirlatma
// listesinden alarmlari yeniden kuruyor. Kullanici uygulamayi acmasa bile.
class HatirlatmaYenileyici : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Sadece bu iki sistem yayini: baska bir niyetle tetiklenirse bir sey yapma.
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        val zamanlayici = EntryPointAccessors
            .fromApplication(context, HatirlatmaGirisi::class.java)
            .zamanlayici()

        val bekleyen = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                zamanlayici.yenidenKur()
            } finally {
                bekleyen.finish()
            }
        }
    }
}
