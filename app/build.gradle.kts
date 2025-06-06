plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp) // Plugin KSP, dodany w libs.versions.toml
}

android {
    namespace = "com.example.cookmate"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.cookmate"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11" // Zmieniono na 1.8 zamiast 11
    }

    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/NOTICE")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/NOTICE.txt")
    }
}

dependencies {
    // Twoje obecne zależności
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Room (baza danych)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Glide (do obsługi obrazów)
    implementation(libs.glide)
    ksp(libs.compiler)

    implementation(libs.cardview)
    implementation(libs.play.services.base) // lub najnowsza wersja
    implementation(libs.appcompat)
    implementation(libs.annotation)
    implementation(libs.material)
    implementation(libs.swiperefreshlayout)

    implementation(libs.play.services.auth)
    implementation(libs.google.api.client.android)
    implementation(libs.google.api.services.calendar)
    implementation(libs.security.crypto)
    implementation(libs.google.api.client.gson)
    implementation(libs.googleid)
}