package com.oguzhanp.motorum.ui.onboarding

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// DataStore diske gercek bir dosya yaziyor; uygulama kapansa da kalir.
// Delegate dosya duzeyinde olmak zorunda: ayni isimle ikinci bir ornek
// acilirsa calisma zamaninda hata verir, bu yazim onu garantiliyor.
val Context.ayarlarDataStore: DataStore<Preferences> by preferencesDataStore(name = "ayarlar")

private val ONBOARDING_BITTI = booleanPreferencesKey("onboarding_bitti")

// AndroidViewModel, ViewModel'in Application tasiyan hali.
// DataStore Context istiyor; Hilt gibi bir enjeksiyon kutuphanesi olmadigi icin
// en kisa yol bu. Normalde ViewModel Android siniflarini tanimamali.
class OnboardingViewModel(uygulama: Application) : AndroidViewModel(uygulama) {

    private val dataStore = uygulama.ayarlarDataStore

    // null = henuz okumadik. false/true = cevap geldi.
    // Uc durumu tek alanda tutuyoruz; MainActivity splash'i null iken bekletiyor.
    private val _onboardingBitti = MutableStateFlow<Boolean?>(null)
    val onboardingBitti = _onboardingBitti.asStateFlow()

    init {
        viewModelScope.launch {
            // first(): akistan ilk degeri al ve bitir. Bayragi baska kimse degistirmiyor,
            // surekli dinlemeye gerek yok.
            _onboardingBitti.value = dataStore.data.first()[ONBOARDING_BITTI] ?: false
        }
    }

    fun tamamla() {
        viewModelScope.launch {
            dataStore.edit { tercihler -> tercihler[ONBOARDING_BITTI] = true }
        }
    }
}
