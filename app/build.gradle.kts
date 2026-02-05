import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.gms.google.services)
    alias(libs.plugins.hilt.android)
    id("kotlin-parcelize")
}

kotlin {
    jvmToolchain(17)

    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

android {
    namespace = "com.mekki.taco"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mekki.taco"
        minSdk = 24
        targetSdk = 36
        versionCode = 8
        versionName = "v0.7.0-beta"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            isDefault = true
        }

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
            versionNameSuffix = ""
            isDefault = true
        }

        create("ai") {
            dimension = "version"
            versionNameSuffix = "-AIunsupportedBuild"
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
    implementation(libs.androidx.compose.material3)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.coroutines.test)
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
    implementation(libs.gson)

    // Desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Glance (Widgets)
    implementation(libs.glance.appwidget)
}