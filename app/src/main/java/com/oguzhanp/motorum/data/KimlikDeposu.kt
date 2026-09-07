package com.oguzhanp.motorum.data

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.tasks.await

class KimlikDeposu(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    val oturumAcik: Boolean get() = auth.currentUser != null

    suspend fun girisYap(eposta: String, sifre: String): String? = try {
        auth.signInWithEmailAndPassword(eposta, sifre).await()
        null
    } catch (hata: Exception) {
        hataMesaji(hata)
    }

    suspend fun uyeOl(eposta: String, sifre: String): String? = try {
        auth.createUserWithEmailAndPassword(eposta, sifre).await()
        null
    } catch (hata: Exception) {
        hataMesaji(hata)
    }

    fun cikisYap() {
        auth.signOut()
    }

    // Firebase'in Ingilizce mesajlari yerine kendi Turkce karsiliklarimiz.
    // Cevrilmeyen durumlar tek bir genel mesaja dusuyor: kullaniciya teknik
    // ayrinti gostermenin faydasi yok.
    private fun hataMesaji(hata: Exception): String = when (hata) {
        is FirebaseNetworkException -> "İnternet bağlantısı yok"
        is FirebaseAuthWeakPasswordException -> "Şifre çok zayıf, en az 6 karakter olmalı"
        is FirebaseAuthInvalidUserException -> "Bu e-posta ile kayıtlı hesap yok"
        is FirebaseAuthInvalidCredentialsException -> "E-posta ya da şifre hatalı"
        is FirebaseAuthUserCollisionException -> "Bu e-posta zaten kayıtlı"
        else -> "Bir sorun oluştu, tekrar deneyin"
    }
}
