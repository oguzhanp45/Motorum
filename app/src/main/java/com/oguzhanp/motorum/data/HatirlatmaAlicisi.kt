package com.oguzhanp.motorum.data

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.oguzhanp.motorum.MainActivity
import com.oguzhanp.motorum.R

// Kanal kimligi ve ek alan adlari tek yerde: kanali MotorumUygulamasi kuruyor,
// alarmi HatirlatmaZamanlayici koyuyor, bildirimi burasi gosteriyor.
internal const val BILDIRIM_KANALI = "bakim_hatirlatma"
internal const val EK_KAYIT_ID = "kayit_id"
internal const val EK_BASLIK = "baslik"

// Alarm caldiginda Android bu sinifi uyandirip onReceive'i cagiriyor.
// Uygulama kapali olsa da calisiyor; o yuzden burada ViewModel, ekran ya da
// oturum bilgisi yok. Gosterecegimiz her sey alarm kurulurken extras'a konuldu.
class HatirlatmaAlicisi : BroadcastReceiver() {

    // notify() Android 13'ten beri POST_NOTIFICATIONS istiyor. Asagida surum ve
    // izin kontrolu var ama lint bileske kosulu goremiyor, o yuzden bastiriliyor.
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val kayitId = intent.getStringExtra(EK_KAYIT_ID) ?: return
        val baslik = intent.getStringExtra(EK_BASLIK).orEmpty()

        // Android 13 oncesinde boyle bir izin yok, bildirim dogrudan gosteriliyor.
        // Sonrasinda izin yoksa notify() sessizce dusuyor; bosuna cagirmiyoruz.
        val izinVar = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
        if (!izinVar) return

        val bildirim = NotificationCompat.Builder(context, BILDIRIM_KANALI)
            .setSmallIcon(R.drawable.ic_hatirlatma)
            .setContentTitle("Bakım zamanı")
            .setContentText(baslik.ifBlank { "Planladığın bakımın vakti geldi." })
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(uygulamayiAcanNiyet(context))
            .build()

        // Her kaydin kendi bildirim kimligi: iki hatirlatma birbirinin uzerine
        // yazmasin.
        NotificationManagerCompat.from(context).notify(kayitId.hashCode(), bildirim)
    }

    private fun uygulamayiAcanNiyet(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
}
