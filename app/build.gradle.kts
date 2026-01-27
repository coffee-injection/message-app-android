plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
}
android {
    namespace = "com.coffeeinjection.message"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.coffeeinjection.message"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("../coffeeInjection.keystore")
            storePassword = "coffee!!"
            keyAlias = "coffeeInjection"
            keyPassword = "coffee!!"
        }
    }

    buildTypes {
        debug {
            // 난독화/리소스 축소 비활성화(기본값이지만 명시해두면 좋습니다)
            isMinifyEnabled = false
            buildConfigField("String", "BASE_URL", "\"http://15.164.112.136:8080/api/v1/\"")
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles("proguard-rules.pro")
            buildConfigField("String", "BASE_URL", "\"http://15.164.112.136:8080/api/v1/\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures{
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.messaging.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.timber)

    // by viewmodel
    implementation("androidx.activity:activity-ktx:1.9.2")
    implementation("androidx.fragment:fragment-ktx:1.8.9")

    // hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")

    // Kotlin + Coroutines + Flow
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // DataStore for token persistence
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // Jetpack navigation
    val nav_version = "2.9.5"
    // Views/Fragments integration
    implementation("androidx.navigation:navigation-fragment:$nav_version")
    implementation("androidx.navigation:navigation-ui:$nav_version")

    implementation ("androidx.core:core-splashscreen:1.0.1")

    // Glide
    implementation ("com.github.bumptech.glide:glide:4.16.0")

    // Retrofit/OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")

    // indicator
    implementation("com.tbuonomo:dotsindicator:4.3")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.8.0"))
    implementation(libs.firebase.analytics)
    implementation(libs.com.google.firebase.firebase.messaging.ktx)


}