package com.oguzhanp.motorum.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import com.oguzhanp.motorum.model.Kayit
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val OTURUM_YOK = "Oturum açık değil"
private const val INTERNET_YOK = "İnternet bağlantısı yok"

data class KayitSonucu(
    val kayitlar: List<Kayit> = emptyList(),
    val hata: String? = null
)

// @Singleton: uygulamada tek ornek. Onceden her ViewModel kendi deposunu
// uretiyordu, yani motorOnbellegi de uc kez ayri tutuluyordu.
@Singleton
class KayitDeposu @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val agDurumu: AgDurumu
) {

    private var motorOnbellegi: Pair<String, String>? = null

    // Kullanicinin ilk motoru okunuyor, yoksa uretiliyor. Kimlik olarak UUID
    // kullaniliyor: motor ekleme geldigi gun yeni motorlar ayni sekilde acilacak
    // ve mevcut kayitlar zaten dogru yolda oldugu icin tasinmayacak.
    private suspend fun motorId(uid: String): String {
        motorOnbellegi?.let { (onbellektekiUid, id) ->
            if (onbellektekiUid == uid) return id
        }

        val motorlar = firestore
            .collection("users").document(uid)
            .collection("motorlar")

        val mevcut = motorlar.limit(1).get().await().documents.firstOrNull()?.id

        val id = mevcut ?: UUID.randomUUID().toString().also { yeniId ->
            motorlar.document(yeniId)
                .set(mapOf("olusturmaMillis" to System.currentTimeMillis()))
                .await()
        }

        // Onbellekte uid ile birlikte tutuluyor: hesap degisirse eski id kullanilmasin.
        motorOnbellegi = uid to id
        return id
    }

    // users/{uid}/motorlar/{motorId}/kayitlar
    // uid her cagrida auth'tan taze okunuyor, sinifta saklanmiyor.
    private suspend fun kayitlarKoleksiyonu(): CollectionReference? {
        val uid = auth.currentUser?.uid ?: return null
        return firestore
            .collection("users").document(uid)
            .collection("motorlar").document(motorId(uid))
            .collection("kayitlar")
    }

    // Source.SERVER sart: varsayilan get() sunucuya ulasamayinca onbellege dusuyor
    // ve bunu HATA olarak degil, gecerli bir sonuc olarak donduruyor. Onbellek bos
    // oldugu icin de "basarili ama bos liste" gibi gorunuyordu. SERVER derken
    // "onbellegi istemiyorum" demis oluyoruz: ulasilamazsa hata firlatiyor.
    suspend fun kayitlariGetir(): KayitSonucu {
        return try {
            val koleksiyon = kayitlarKoleksiyonu() ?: return KayitSonucu(hata = OTURUM_YOK)
            val anlik = koleksiyon.get(Source.SERVER).await()
            KayitSonucu(
                // Cevrilemeyen belge eleniyor: tek bozuk kayit yuzunden liste comesin.
                kayitlar = anlik.documents.mapNotNull { belge ->
                    belge.toObject(KayitBelgesi::class.java)?.kayidaCevir()
                }
            )
        } catch (hata: Exception) {
            KayitSonucu(hata = hataMesaji(hata))
        }
    }

    // Yazmadan once ag kontrolu sart: Firestore cevrimdisiyken hata FIRLATMIYOR,
    // yazmayi kuyruga alip susuyor. await orada askida kalirdi ve try/catch'in
    // yakalayacagi bir sey olmazdi. Sessizligi cevaba ceviren sey bu kontrol.
    suspend fun kaydet(kayit: Kayit): String? {
        if (!agDurumu.internetVar()) return INTERNET_YOK
        return try {
            val koleksiyon = kayitlarKoleksiyonu() ?: return OTURUM_YOK
            // set ayni id'ye yazinca belgeyi bastan yaziyor: ekleme ve duzenleme ayni fonksiyon.
            koleksiyon.document(kayit.id).set(kayit.belgeyeCevir()).await()
            null
        } catch (hata: Exception) {
            hataMesaji(hata)
        }
    }

    suspend fun sil(id: String): String? {
        if (!agDurumu.internetVar()) return INTERNET_YOK
        return try {
            val koleksiyon = kayitlarKoleksiyonu() ?: return OTURUM_YOK
            koleksiyon.document(id).delete().await()
            null
        } catch (hata: Exception) {
            hataMesaji(hata)
        }
    }

    private fun hataMesaji(hata: Exception): String = when {
        hata is FirebaseFirestoreException &&
                hata.code == FirebaseFirestoreException.Code.UNAVAILABLE -> INTERNET_YOK

        hata is FirebaseFirestoreException &&
                hata.code == FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "Bu veriye erişim izniniz yok"

        else -> "Bir sorun oluştu, tekrar deneyin"
    }
}
