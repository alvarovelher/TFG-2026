plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.gasolineras_espana"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.gasolineras_espana"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Key de Routes desde codigo Java
        buildConfigField(
            "String",
            "ROUTES_API_KEY",
            "\"AIzaSyBjOvdFwiL2dF-c67PnWu7h70V2UQxx7s4\""
        )
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

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation("com.airbnb.android:lottie:6.1.0")
    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("com.google.android.gms:play-services-maps:19.2.0")
    implementation("com.google.maps.android:android-maps-utils:3.20.1")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}