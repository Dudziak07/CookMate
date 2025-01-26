plugins {
    alias(libs.plugins.android.application)
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
    ksp(libs.room.compiler)

    // Glide (do obsługi obrazów)
    implementation(libs.glide)
    ksp(libs.compiler)
}