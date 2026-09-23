package com.oguzhanp.motorum.core.tasarim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oguzhanp.motorum.data.ayarlar.AyarlarDeposu
import com.oguzhanp.motorum.model.TemaSecimi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// Uygulamanin en tepesindeki tema. Activity'ye bagli, tum ekranlar bunun
// altinda ciziliyor; ayarlardan secim degisince burasi aninda yeni degeri veriyor.
@HiltViewModel
class TemaViewModel @Inject constructor(
    ayarlarDeposu: AyarlarDeposu
) : ViewModel() {

    // null = diskten henuz okunmadi. Splash bu sure ekranda kaliyor ki
    // karanlik secen kullanici acilista bir an acik tema gormesin.
    // Eagerly: ekran cizilmeden once okumaya baslasin.
    val secim: StateFlow<TemaSecimi?> = ayarlarDeposu.temaSecimi
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
