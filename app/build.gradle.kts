plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.example.eternalwayfinder"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.eternalwayfinder"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation (libs.play.services.maps)
    implementation (libs.play.services.maps.v1810)
    implementation (libs.places)
    implementation (libs.volley)
    implementation (libs.play.services.location)
    implementation (libs.play.services.location.v1901)
    implementation (libs.gms.play.services.maps.v1810)
    implementation (libs.gms.play.services.location.v1901)
    implementation (libs.places.v1810)
    implementation (libs.play.services.location)
    implementation (libs.play.services.maps.vlatestversion)
    implementation (libs.places.vlatestversion)
    implementation (libs.android.maps.utils)
    implementation (libs.play.services.maps.v1802)
    implementation (libs.places.v310)
    implementation (libs.play.services.location.vlatestversion)
    implementation(libs.activity)
    implementation (libs.gms.play.services.maps.v1802)
    implementation (libs.google.play.services.location.v1901)
    implementation (libs.places.v260)
    implementation(libs.ui.text.android)
    implementation(libs.litert.support.api)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.gson)
    implementation(libs.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}