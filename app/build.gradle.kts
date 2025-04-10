plugins {
    alias(libs.plugins.android.application)
//    id("com.chaquo.python")
}

android {
    namespace = "com.example.bigchallengesproject"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.bigchallengesproject"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

//        ndk {
//            abiFilters += listOf("arm64-v8a")
//        }
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
}

//chaquopy {
//    defaultConfig {
//        version = "3.8"
//        pip {
//            install("libs/pyclipper-1.3.0.post6-cp38-cp38-win_amd64.whl")
//            install("-r", "requirements.txt")
//        }
//    }
//}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(project(":opencv"))
//    implementation(libs.microsoft.onnxruntime.android)
    implementation(libs.core.splashscreen)
    implementation(libs.gridlayout)
    implementation(libs.poi)
    implementation(libs.poi.ooxml)
    implementation(libs.retrofit)
    implementation(libs.retrofit2.converter.gson)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}