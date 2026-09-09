package com.oguzhanp.motorum.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.oguzhanp.motorum.model.Motor
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// Firestore toplu yazmada en fazla 500 islem aliyor. 400'lu parcalar guvenli
// bir sinir birakiyor; normal kullanicida tek parca cikacagi icin islem yine
// tek batch, yani ya hep ya hic uygulaniyor.
private const val TOPLU_SILME_PARCASI = 400

data class MotorSonucu(
    val motorlar: List<Motor> = emptyList(),
    val hata: String? = null
)

@Singleton
class MotorDeposu @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val agDurumu: AgDurumu,
    private val ayarlarDeposu: AyarlarDeposu
) {

    private var onbellek: Pair<String, String>? = null

    private fun motorlarKoleksiyonu(uid: String): CollectionReference = firestore
        .collection(KOLEKSIYON_KULLANICILAR).document(uid)
        .collection(KOLEKSIYON_MOTORLAR)

    suspend fun motorlariGetir(): MotorSonucu {
        return try {
            val uid = auth.currentUser?.uid ?: return MotorSonucu(hata = OTURUM_YOK)
            MotorSonucu(motorlar = motorlariOku(uid))
        } catch (hata: Exception) {
            MotorSonucu(hata = hataMesaji(hata))
        }
    }

    suspend fun seciliMotorId(): String? {
        val uid = auth.currentUser?.uid ?: return null
        onbellek?.let { (onbellektekiUid, id) ->
            if (onbellektekiUid == uid) return id
        }

        val kayitliId = ayarlarDeposu.seciliMotorId(uid)
        val motorlar = motorlariOku(uid)

        // Motoru yoksa null donuyoruz. Eskiden burada sessizce bir "bilgisiz motor"
        // yaratiliyordu; Motor Ekle gelince o koltuk degnegine gerek kalmadi.
        // Bir okuma islemi kalici veri yaratmamali.
        val secili = motorlar.firstOrNull { it.id == kayitliId }
            ?: motorlar.minByOrNull { it.olusturmaMillis }
            ?: return null

        ayarlarDeposu.seciliMotoruYaz(uid, secili.id)
        onbellek = uid to secili.id
        return secili.id
    }

    // Cikista cagriliyor: cozulmus motor kimligi bellekte kalmasin, bir sonraki
    // giriste bastan cozulsun.
    fun onbellegiTemizle() {
        onbellek = null
    }

    suspend fun seciliMotoruDegistir(motorId: String) {
        val uid = auth.currentUser?.uid ?: return
        // Once bellek, sonra disk. Diski beklerken gelen bir okuma eski motoru
        // gormesin diye sira boyle.
        onbellek = uid to motorId
        ayarlarDeposu.seciliMotoruYaz(uid, motorId)
    }

    suspend fun kaydet(motor: Motor): String? {
        if (!agDurumu.internetVar()) return INTERNET_YOK
        return try {
            val uid = auth.currentUser?.uid ?: return OTURUM_YOK
            motorlarKoleksiyonu(uid).document(motor.id).set(motor.belgeyeCevir()).await()
            null
        } catch (hata: Exception) {
            hataMesaji(hata)
        }
    }

    suspend fun sil(motorId: String): String? {
        if (!agDurumu.internetVar()) return INTERNET_YOK
        return try {
            val uid = auth.currentUser?.uid ?: return OTURUM_YOK
            val motorBelgesi = motorlarKoleksiyonu(uid).document(motorId)

            val kayitlar = motorBelgesi
                .collection(KOLEKSIYON_KAYITLAR)
                .get(Source.SERVER).await()
                .documents

            kayitlar.chunked(TOPLU_SILME_PARCASI).forEach { parca ->
                val toplu = firestore.batch()
                parca.forEach { toplu.delete(it.reference) }
                toplu.commit().await()
            }

            motorBelgesi.delete().await()

            if (ayarlarDeposu.seciliMotorId(uid) == motorId) {
                ayarlarDeposu.seciliMotoruTemizle(uid)
            }
            onbellek = null
            null
        } catch (hata: Exception) {
            hataMesaji(hata)
        }
    }

    private suspend fun motorlariOku(uid: String): List<Motor> =
        motorlarKoleksiyonu(uid).get(Source.SERVER).await()
            .documents.mapNotNull { belge ->
                belge.toObject(MotorBelgesi::class.java)?.motoraCevir()
            }

    // Silme uyarisinda gercek sayiyi soyleyebilmek icin. count() bir toplama
    // sorgusu: belgeleri indirmiyor, sunucuda sayip tek bir sayi donduruyor.
    suspend fun kayitSayisi(motorId: String): Int {
        return try {
            val uid = auth.currentUser?.uid ?: return 0
            motorlarKoleksiyonu(uid).document(motorId)
                .collection(KOLEKSIYON_KAYITLAR)
                .count().get(AggregateSource.SERVER).await()
                .count.toInt()
        } catch (hata: Exception) {
            0
        }
    }
}
