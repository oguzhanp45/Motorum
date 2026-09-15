package com.oguzhanp.motorum.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
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

data class FotografSonucu(
    val veri: String? = null,
    val hata: String? = null
)

// Motor kaydedilirken fotografa ne yapilacagi. Uc durum birbirini disliyor.
// Bunu "degisti mi" ve "yeni veri" diye iki ayri parametreyle anlatsaydik
// "degisti ama veri yok" gibi anlamsiz bir bilesim de mumkun olurdu.
sealed interface FotografIslemi {
    data object Dokunma : FotografIslemi
    data class Degistir(val tamBoy: String) : FotografIslemi
    data object Kaldir : FotografIslemi
}

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
        // Okumalarda da ag bekcisi var: Source.SERVER cevrimdisiyken
        // yakalanamayan bir hata firlatiyor. Ayrintisi KayitDeposu icinde.
        if (!agDurumu.internetVar()) return MotorSonucu(hata = INTERNET_YOK)

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

        // Bellekteki cevap yukarida dondu, buradan sonrasi sunucuya gidiyor.
        // Cevrimdisiyken null: yanlis motor uydurmaktansa "bilmiyoruz" demek.
        if (!agDurumu.internetVar()) return null

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

    suspend fun kaydet(
        motor: Motor,
        fotograf: FotografIslemi = FotografIslemi.Dokunma
    ): String? {
        if (!agDurumu.internetVar()) return INTERNET_YOK
        return try {
            val uid = auth.currentUser?.uid ?: return OTURUM_YOK
            val motorBelgesi = motorlarKoleksiyonu(uid).document(motor.id)

            // Motor bilgisi ile fotograf tek toplu islemde gidiyor: ikisi birden
            // yazilir ya da hicbiri yazilmaz. Ayri ayri yazsaydik arada baglanti
            // koptugunda kartta onizleme gorunup detayda fotograf bulunamazdi.
            val toplu = firestore.batch()
            toplu.set(motorBelgesi, motor.belgeyeCevir())

            when (fotograf) {
                FotografIslemi.Dokunma -> Unit
                FotografIslemi.Kaldir -> toplu.delete(fotografBelgesi(motorBelgesi))
                is FotografIslemi.Degistir -> toplu.set(
                    fotografBelgesi(motorBelgesi),
                    mapOf(ALAN_VERI to fotograf.tamBoy)
                )
            }

            toplu.commit().await()
            null
        } catch (hata: Exception) {
            hataMesaji(hata)
        }
    }

    // Motorun fotografi tek alanli bir belgede duruyor, o yuzden DTO yazmadik:
    // alani dogrudan okuyoruz. Firestore yine data/ katmanindan disari cikmiyor.
    suspend fun fotografGetir(motorId: String): FotografSonucu {
        if (!agDurumu.internetVar()) return FotografSonucu(hata = INTERNET_YOK)

        return try {
            val uid = auth.currentUser?.uid ?: return FotografSonucu(hata = OTURUM_YOK)
            val belge = fotografBelgesi(motorlarKoleksiyonu(uid).document(motorId))
                .get(Source.SERVER).await()
            FotografSonucu(veri = belge.getString(ALAN_VERI))
        } catch (hata: Exception) {
            FotografSonucu(hata = hataMesaji(hata))
        }
    }

    private fun fotografBelgesi(motorBelgesi: DocumentReference): DocumentReference =
        motorBelgesi.collection(KOLEKSIYON_MEDYA).document(BELGE_FOTOGRAF)

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

            // Fotograf da motora ait. Olmayan bir belgeyi silmek zararsiz,
            // o yuzden once var mi diye bakmiyoruz.
            fotografBelgesi(motorBelgesi).delete().await()

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

    // Siralama burada: Motorlarim listesi ve secim paneli ayni sirayi
    // gosteriyor. Iki ekranda ayri ayri siralasaydik birini degistirmek
    // digerini sessizce eski birakirdi.
    private suspend fun motorlariOku(uid: String): List<Motor> =
        motorlarKoleksiyonu(uid).get(Source.SERVER).await()
            .documents.mapNotNull { belge ->
                belge.toObject(MotorBelgesi::class.java)?.motoraCevir()
            }
            .sortedBy { it.olusturmaMillis }

    // Silme uyarisinda gercek sayiyi soyleyebilmek icin. count() bir toplama
    // sorgusu: belgeleri indirmiyor, sunucuda sayip tek bir sayi donduruyor.
    suspend fun kayitSayisi(motorId: String): Int {
        // Cevrimdisiyken 0. Asagidaki catch de zaten 0 donduruyordu; bu
        // kontrol sadece cokmeyi engelliyor.
        if (!agDurumu.internetVar()) return 0

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
