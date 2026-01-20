import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.gms.google.services)
}

android {
    namespace = "com.mekki.taco"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mekki.taco"
        minSdk = 24
        targetSdk = 36
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
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    flavorDimensions += "version"
    productFlavors {
        create("standard") {
            dimension = "version"
            versionNameSuffix = "-standard"
        }
        create("ai") {
            dimension = "version"
            versionNameSuffix = "-ai"
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.foundation.layout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Compose BOM + libs
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.core)

    debugImplementation(libs.compose.ui.tooling)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)

    // AndroidX
    implementation(libs.navigation.compose)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore + 3rd party
    implementation(libs.datastore.preferences)
    implementation(libs.reorderable)

    // AI
    "aiImplementation"(libs.mlkit.text.recognition)
    "aiImplementation"(platform(libs.firebase.bom))
    "aiImplementation"(libs.firebase.vertexai)
    "aiImplementation"(libs.camera.core)
    "aiImplementation"(libs.camera.camera2)
    "aiImplementation"(libs.camera.lifecycle)
    "aiImplementation"(libs.camera.view)
    "aiImplementation"(libs.accompanist.permissions)
    "aiImplementation"(libs.coroutines.play.services)

    // Gson
    implementation("com.google.code.gson:gson:2.13.2")

    coreLibraryDesugaring(libs.desugar.jdk.libs)
}