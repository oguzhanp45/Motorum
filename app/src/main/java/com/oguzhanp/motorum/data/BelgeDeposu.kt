package com.oguzhanp.motorum.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.oguzhanp.motorum.model.Belge
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class BelgeSonucu(
    val belgeler: List<Belge> = emptyList(),
    val hata: String? = null,
    val motorYok: Boolean = false
)

// Belgeler de kayitlar gibi motorun altinda: users/{uid}/motorlar/{motorId}/belgeler.
// Ag kontrolu ve Source.SERVER kurali KayitDeposu ile ayni; gerekceleri orada.
@Singleton
class BelgeDeposu @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val agDurumu: AgDurumu,
    private val motorDeposu: MotorDeposu
) {

    private fun belgelerKoleksiyonu(uid: String, motorId: String): CollectionReference =
        firestore
            .collection(KOLEKSIYON_KULLANICILAR).document(uid)
            .collection(KOLEKSIYON_MOTORLAR).document(motorId)
            .collection(KOLEKSIYON_BELGELER)

    suspend fun belgeleriGetir(): BelgeSonucu {
        if (!agDurumu.internetVar()) return BelgeSonucu(hata = INTERNET_YOK)
        return try {
            val uid = auth.currentUser?.uid ?: return BelgeSonucu(hata = OTURUM_YOK)
            val motorId = motorDeposu.seciliMotorId() ?: return BelgeSonucu(motorYok = true)
            val anlik = belgelerKoleksiyonu(uid, motorId).get(Source.SERVER).await()
            BelgeSonucu(
                belgeler = anlik.documents.mapNotNull { belge ->
                    belge.toObject(BelgeVerisi::class.java)?.belgeyeCevir()
                }
            )
        } catch (hata: Exception) {
            BelgeSonucu(hata = hataMesaji(hata))
        }
    }

    // set ayni kimlige yazinca belgeyi bastan yaziyor: ekleme ve duzenleme ayni.
    suspend fun kaydet(belge: Belge): String? {
        if (!agDurumu.internetVar()) return INTERNET_YOK
        return try {
            val uid = auth.currentUser?.uid ?: return OTURUM_YOK
            val motorId = motorDeposu.seciliMotorId() ?: return "Önce bir motor eklemelisin"
            belgelerKoleksiyonu(uid, motorId).document(belge.id).set(belge.veriyeCevir()).await()
            null
        } catch (hata: Exception) {
            hataMesaji(hata)
        }
    }

    suspend fun sil(id: String): String? {
        if (!agDurumu.internetVar()) return INTERNET_YOK
        return try {
            val uid = auth.currentUser?.uid ?: return OTURUM_YOK
            val motorId = motorDeposu.seciliMotorId() ?: return OTURUM_YOK
            belgelerKoleksiyonu(uid, motorId).document(id).delete().await()
            null
        } catch (hata: Exception) {
            hataMesaji(hata)
        }
    }
}
