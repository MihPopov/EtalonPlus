plugins {
    alias(libs.plugins.android.application)
    id("com.chaquo.python")
}

android {
    namespace = "com.cubably.gradeplus"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cubably.gradeplus"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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

chaquopy {
    defaultConfig {
        version = "3.12"
        pip {
            install("g4f==v7.2.5")
            install("pillow")
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.core.splashscreen)
    implementation(libs.gridlayout)
    implementation(libs.poi)
    implementation(libs.poi.ooxml)
    implementation(libs.mpandroidchart)
    implementation(libs.work.runtime)
    implementation(libs.gson)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}