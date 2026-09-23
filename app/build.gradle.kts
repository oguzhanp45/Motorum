import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
}

// OpenWeather anahtari local.properties'ten okunuyor; o dosya .gitignore'da,
// yani depoya girmiyor. Dosya yoksa bos anahtarla derleniyor ki proje baska
// bir makinede de acilabilsin.
val yerelOzellikler = Properties().apply {
    val dosya = rootProject.file("local.properties")
    if (dosya.exists()) dosya.inputStream().use { load(it) }
}
val openWeatherAnahtari: String = yerelOzellikler.getProperty("OPENWEATHER_KEY") ?: ""

android {
    namespace = "com.oguzhanp.motorum"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.oguzhanp.motorum"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Anahtar BuildConfig.OPENWEATHER_KEY olarak koda gomuluyor. Bu onu
        // gizlemiyor (APK'yi acan bulabilir), sadece depoya girmesini onluyor.
        buildConfigField("String", "OPENWEATHER_KEY", "\"$openWeatherAnahtari\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        // buildConfigField kullanabilmek icin acik olmasi gerekiyor.
        buildConfig = true
    }
}

// Java 11 yukarida ayarli, Kotlin'in varsayilani ise cok daha yeni.
// Yerlesik Kotlin bu ikisini kendisi hizaliyordu; eklentiyi elle uygulayinca
// hizalama da bize kaldi.
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.activity.compose)
    // Uygulama dili (AppCompatDelegate.setApplicationLocales) Android 13 oncesinde
    // bu kutuphaneyle calisiyor.
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.play.services.location)
    implementation(libs.lottie.compose)
    ksp(libs.hilt.android.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
