plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.fastvlm05b"
    compileSdk = 36

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.fastvlm05b"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // NPU only supports arm64-v8a
        ndk { abiFilters.add("arm64-v8a") }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    dynamicFeatures.add(":litert_npu_runtime_libraries:qualcomm_runtime_v79")
    
    // // AI packs for AOT models only
    // assetPacks.add(":ai_packs:my_model")

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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    
    // LiteRT and LiteRT-LM
    implementation(libs.litert)
    implementation(libs.litertlm)
    implementation(libs.coil)
    implementation(libs.activity.ktx)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    
    // NPU runtime libraries
    implementation(project(":litert_npu_runtime_libraries:runtime_strings"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}