package com.oguzhanp.motorum.data.hatirlatma

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.oguzhanp.motorum.MainActivity
import com.oguzhanp.motorum.R
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.Calendar
import java.util.Locale
import java.util.Date
import java.text.SimpleDateFormat

// Kanal kimligi ve ek alan adlari tek yerde: kanali MotorumUygulamasi kuruyor,
// alarmi HatirlatmaZamanlayici koyuyor, bildirimi burasi gosteriyor.
internal const val BILDIRIM_KANALI = "bakim_hatirlatma"
// Belgeler ayri kanalda: kullanici telefon ayarlarindan birini kapatip
// digerini acik tutabilsin.
internal const val BELGE_KANALI = "belge_hatirlatma"
internal const val EK_KAYIT_ID = "kayit_id"
internal const val EK_HATIRLATMA = "hatirlatma"
internal const val EK_MOTOR_ID = "motor_id"

// Niyetin ne istedigi. Alarm caldi / "1 hafta ertele" / "Yaptirdim" /
// bildirimin govdesine dokunuldu (uygulamada paneli ac).
internal const val EYLEM_CAL = "com.oguzhanp.motorum.HATIRLATMA_CAL"
internal const val EYLEM_ERTELE = "com.oguzhanp.motorum.HATIRLATMA_ERTELE"
internal const val EYLEM_YAPTIRDIM = "com.oguzhanp.motorum.HATIRLATMA_YAPTIRDIM"
internal const val EYLEM_PANEL = "com.oguzhanp.motorum.HATIRLATMA_PANEL"
// Belge bildirimine dokunuldu: Belgeler sayfasini ac.
internal const val EYLEM_BELGELER = "com.oguzhanp.motorum.BELGELER"

// Her kaydin kendi bildirimi: iki hatirlatma birbirinin uzerine yazmasin.
internal fun bildirimKimligi(kayitId: String): Int = kayitId.hashCode()

private val json = Json { ignoreUnknownKeys = true }

// Bildirimin govdesine dokunulunca uygulamanin acmasi gereken yer: bakimda
// o kaydin paneli, belgede Belgeler sayfasi (kayitId bos).
data class HatirlatmaIstegi(val kayitId: String, val motorId: String, val belgeler: Boolean = false)

// MainActivity kullaniyor: niyet bildirimden geldiyse istegi cikariyor.
fun Intent.hatirlatmaIstegi(): HatirlatmaIstegi? {
    val motorId = getStringExtra(EK_MOTOR_ID) ?: return null
    return when (action) {
        EYLEM_PANEL -> HatirlatmaIstegi(getStringExtra(EK_KAYIT_ID) ?: return null, motorId)
        EYLEM_BELGELER -> HatirlatmaIstegi(kayitId = "", motorId = motorId, belgeler = true)
        else -> null
    }
}

// Alici Android'in urettigi bir sinif; Hilt'in yapici enjeksiyonu burada
// calismiyor. Ihtiyaci olan nesneleri uygulamanin Hilt kabindan bu kapiyla aliyor.
@EntryPoint
@InstallIn(SingletonComponent::class)
interface HatirlatmaGirisi {
    fun zamanlayici(): HatirlatmaZamanlayici
    fun depo(): HatirlatmaDeposu
}

// Alarm caldiginda ve bildirimdeki iki dugmeye ("Yaptirdim", "1 hafta ertele")
// basilinca Android bu sinifi uyandiriyor. Uygulama kapali olsa da calisiyor.
class HatirlatmaAlicisi : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val giris = EntryPointAccessors.fromApplication(context, HatirlatmaGirisi::class.java)

        // Depo diskten okuyor, erteleme buluta yaziyor: ikisi de zaman alan isler.
        // goAsync alicinin omrunu is bitene kadar uzatiyor (en fazla ~10 sn).
        val bekleyen = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    // Iki dugme de uygulamayi acmiyor: bildirim kapaniyor, is arkada bitiyor.
                    EYLEM_ERTELE, EYLEM_YAPTIRDIM -> {
                        val kayit = intent.getStringExtra(EK_HATIRLATMA)
                            ?.let { runCatching { json.decodeFromString(HatirlatmaKaydi.serializer(), it) }.getOrNull() }
                            ?: return@launch
                        NotificationManagerCompat.from(context).cancel(bildirimKimligi(kayit.kayitId))
                        if (intent.action == EYLEM_ERTELE) giris.zamanlayici().ertele(kayit)
                        else giris.zamanlayici().yapildiIsaretle(kayit)
                    }

                    else -> {
                        val kayitId = intent.getStringExtra(EK_KAYIT_ID) ?: return@launch
                        val kayit = giris.depo().bul(kayitId) ?: return@launch
                        if (kayit.tur == TUR_BELGE) {
                            belgeBildirimiGoster(context, kayit)
                            // Yenileme aciksa bir sonraki donem kuruluyor, degilse siliniyor.
                            giris.zamanlayici().belgeCaldi(kayit)
                        } else {
                            // Caldi: telefondaki listeden cikiyor. Ertelenirse yeniden giriyor.
                            giris.depo().sil(kayitId)
                            bildirimGoster(context, kayit)
                        }
                    }
                }
            } finally {
                bekleyen.finish()
            }
        }
    }
}

// notify() Android 13'ten beri POST_NOTIFICATIONS istiyor. Surum ve izin
// kontrolu var ama lint bileske kosulu goremiyor, o yuzden bastiriliyor.
@SuppressLint("MissingPermission")
private fun bildirimGoster(context: Context, kayit: HatirlatmaKaydi) {
    val izinVar = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    if (!izinVar) return

    // Metinler uygulamanin dilinde: bildirimi sistem gosteriyor ama yazilari
    // biz uretiyoruz, o yuzden dili burada kendimiz sececek bir context kuruyoruz.
    val yerel = dilBaglami(context)
    val tur = kayit.bakimTuru.ifBlank { yerel.getString(R.string.bakim_varsayilan) }
    val baslik = yerel.getString(R.string.bildirim_bakim_baslik, tur)
    // "Honda CB500F · 12 Mart'taki bakımda bunu kurmuştun."
    val metin = yerel.getString(
        R.string.bildirim_bakim_metin,
        kayit.motorAdi,
        tarihliBakim(yerel, kayit.bakimTarihi)
    )
    val kayitJson = json.encodeToString(HatirlatmaKaydi.serializer(), kayit)

    val bildirim = NotificationCompat.Builder(context, BILDIRIM_KANALI)
        .setSmallIcon(R.drawable.ic_hatirlatma)
        .setContentTitle(baslik)
        .setContentText(metin)
        // Uzun motor adinda metin kesilmesin: acilinca tamami gorunuyor.
        .setStyle(NotificationCompat.BigTextStyle().bigText(metin))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setContentIntent(paneliAcanNiyet(context, kayit))
        .addAction(
            0,
            yerel.getString(R.string.yaptirdim),
            yaptirdimNiyeti(context, kayit.kayitId, kayitJson)
        )
        .addAction(
            0,
            yerel.getString(R.string.bir_hafta_ertele),
            erteleNiyeti(context, kayit.kayitId, kayitJson)
        )
        .build()

    NotificationManagerCompat.from(context).notify(bildirimKimligi(kayit.kayitId), bildirim)
}

// "Sigorta 7 gun sonra bitiyor" / "Honda CB500F · Bitis: 22 Eylul 2026".
// Dugme yok: belgede yapilacak is yeni tarihi girmek, o da uygulamada.
@SuppressLint("MissingPermission")
private fun belgeBildirimiGoster(context: Context, kayit: HatirlatmaKaydi) {
    val izinVar = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    if (!izinVar) return

    val yerel = dilBaglami(context)
    val kalan = kalanGun(kayit.belgeBitis)
    val baslik = when {
        kalan <= 0 -> yerel.getString(R.string.bildirim_belge_bugun, kayit.belgeAdi)
        kalan == 1 -> yerel.getString(R.string.bildirim_belge_yarin, kayit.belgeAdi)
        else -> yerel.resources.getQuantityString(
            R.plurals.bildirim_belge_gun, kalan, kayit.belgeAdi, kalan
        )
    }
    // Ay adi ve tarih sirasi da uygulamanin dilinde.
    val tarih = SimpleDateFormat("d MMMM yyyy", yerelDil(yerel)).format(Date(kayit.belgeBitis))
    val govde = yerel.getString(R.string.bildirim_belge_metin, kayit.motorAdi, tarih)
    val metin = if (kayit.belgeYenileAy != null) {
        yerel.getString(R.string.bildirim_belge_yenileme, govde)
    } else {
        govde
    }

    val bildirim = NotificationCompat.Builder(context, BELGE_KANALI)
        .setSmallIcon(R.drawable.ic_hatirlatma)
        .setContentTitle(baslik)
        .setContentText(metin)
        .setStyle(NotificationCompat.BigTextStyle().bigText(metin))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setContentIntent(belgeleriAcanNiyet(context, kayit))
        .build()

    NotificationManagerCompat.from(context).notify(bildirimKimligi(kayit.kayitId), bildirim)
}

private fun belgeleriAcanNiyet(context: Context, kayit: HatirlatmaKaydi): PendingIntent =
    PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java)
            .setAction(EYLEM_BELGELER)
            .setData((HATIRLATMA_ADRESI + kayit.kayitId).toUri())
            .putExtra(EK_MOTOR_ID, kayit.motorId)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

// Bugunden bitise kalan takvim gunu (ogle saatine cekilerek, saat farki
// sonucu kaydirmasin).
private fun kalanGun(bitis: Long): Int {
    fun ogle(millis: Long) = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 12); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    return ((ogle(bitis) - ogle(System.currentTimeMillis())) / (24L * 60 * 60 * 1000)).toInt()
}

// Bildirimin govdesine dokununca uygulama aciliyor ve o kaydin hatirlatma
// paneli geliyor. SINGLE_TOP + CLEAR_TOP: uygulama aciksa yeniden
// kurulmuyor, niyet onNewIntent'e dusuyor.
private fun paneliAcanNiyet(context: Context, kayit: HatirlatmaKaydi): PendingIntent =
    PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java)
            .setAction(EYLEM_PANEL)
            .setData((HATIRLATMA_ADRESI + kayit.kayitId).toUri())
            .putExtra(EK_KAYIT_ID, kayit.kayitId)
            .putExtra(EK_MOTOR_ID, kayit.motorId)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

// Iki dugme de ayni aliciya gidiyor; ne istendigini eylem soyluyor. Eylemler
// farkli oldugu icin ikisi ayri PendingIntent sayiliyor.
private fun yaptirdimNiyeti(context: Context, kayitId: String, kayitJson: String): PendingIntent =
    dugmeNiyeti(context, EYLEM_YAPTIRDIM, kayitId, kayitJson)

private fun erteleNiyeti(context: Context, kayitId: String, kayitJson: String): PendingIntent =
    dugmeNiyeti(context, EYLEM_ERTELE, kayitId, kayitJson)

private fun dugmeNiyeti(context: Context, eylem: String, kayitId: String, kayitJson: String): PendingIntent =
    PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, HatirlatmaAlicisi::class.java)
            .setAction(eylem)
            .setData((HATIRLATMA_ADRESI + kayitId).toUri())
            .putExtra(EK_HATIRLATMA, kayitJson),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

// Uygulamanin secili dilini tasiyan context. Bildirim, uygulama kapaliyken
// de olusuyor; Application context telefonun dilini tasiyabiliyor, biz
// kullanicinin sectigi dili istiyoruz.
private fun dilBaglami(context: Context): Context {
    val diller = AppCompatDelegate.getApplicationLocales()
    if (diller.isEmpty) return context
    val ayar = Configuration(context.resources.configuration).apply {
        setLocales(diller.unwrap() as LocaleList)
    }
    return context.createConfigurationContext(ayar)
}

private fun yerelDil(context: Context): Locale =
    context.resources.configuration.locales[0]

// "12 Mart'taki bakımda". Turkcede ek ayin son sesine gore degisiyor
// (Mart'taki, Haziran'daki, Eylul'deki); ay adlari sabit oldugu icin tablo.
// Baska yildaysa yil sayisina ek bulmak yerine "tarihindeki" diyoruz.
// Turkce disinda ek yok: tarih oldugu gibi yaziliyor.
private fun tarihliBakim(context: Context, millis: Long): String {
    val takvim = Calendar.getInstance().apply { timeInMillis = millis }
    val buYil = Calendar.getInstance().get(Calendar.YEAR)
    val ayniYil = takvim.get(Calendar.YEAR) == buYil
    val gun = takvim.get(Calendar.DAY_OF_MONTH)

    val tarih = if (yerelDil(context).language == "tr") {
        val (ay, ek) = AYLAR[takvim.get(Calendar.MONTH)]
        if (ayniYil) "$gun $ay'$ek" else "$gun $ay ${takvim.get(Calendar.YEAR)}"
    } else {
        val kalip = if (ayniYil) "d MMMM" else "d MMMM yyyy"
        SimpleDateFormat(kalip, yerelDil(context)).format(Date(millis))
    }
    return context.getString(
        if (ayniYil) R.string.bakim_tarihli else R.string.bakim_tarihli_eski,
        tarih
    )
}

private val AYLAR = listOf(
    "Ocak" to "taki", "Şubat" to "taki", "Mart" to "taki", "Nisan" to "daki",
    "Mayıs" to "taki", "Haziran" to "daki", "Temmuz" to "daki", "Ağustos" to "taki",
    "Eylül" to "deki", "Ekim" to "deki", "Kasım" to "daki", "Aralık" to "taki"
)
