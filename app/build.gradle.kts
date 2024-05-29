
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.kapt)
    id("kotlin-parcelize")
    kotlin("plugin.serialization") version "2.0.0"
}

android {
    namespace = "dev.kokorev.cryptoview"
    compileSdk = 34

    defaultConfig {
        applicationId = "dev.kokorev.cryptoview"
        minSdk = 26
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

    flavorDimensions += "version"

    productFlavors {
        create("basic") {
            // Assigns this product flavor to the "version" flavor dimension.
            // If you are using only one dimension, this property is optional,
            // and the plugin automatically assigns all the module's flavors to
            // that dimension.
            dimension = "version"
            applicationIdSuffix = ".basic"
            versionNameSuffix = "-basic"
        }
        create("full") {
            dimension = "version"
            applicationIdSuffix = ".full"
            versionNameSuffix = "-full"
        }
    }

//    compileOptions.incremental = false

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":binance_api"))
    implementation(project(":cmc_api"))
    implementation(project(":coin_paprika_api"))
    implementation(project(":token_metrics_api"))
    implementation(project(":room_db"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.viewPager2)

    implementation(libs.work)
    implementation(libs.work.rxjava3)

    implementation(libs.glide)
    kapt(libs.glide.annotation.processor)

    // airbnb paris lets you change view style programmatically
    implementation(libs.airbnb.paris)
    kapt(libs.airbnb.paris.processor)
    
    // retrofit for error handling
    implementation(libs.retrofit)

    implementation(libs.dagger)
    kapt(libs.daggerCompiler)

    implementation(libs.rxandroid)

    implementation(libs.highcharts)

    implementation(libs.moshi)
    implementation(libs.kotlinx.json)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
