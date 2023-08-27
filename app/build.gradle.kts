plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.sqldelight)
}

android {
    namespace = "org.jraf.android.renotify"

    compileSdk = 33

    defaultConfig {
        applicationId = "org.jraf.android.renotify"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("SIGNING_STORE_PATH") ?: ".")
            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xcontext-receivers")
    }
}

dependencies {
    implementation(libs.google.material)

    implementation(libs.timber)

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.util)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)

    implementation(libs.sqldelight.androidDriver)
    implementation(libs.sqldelight.coroutines)
}

sqldelight {
    database("Database") {
        schemaOutputDirectory = file("src/main/sqldelight/databases")
    }
}

// To release:
// SIGNING_STORE_PATH=path/to/upload.keystore SIGNING_STORE_PASSWORD=password SIGNING_KEY_ALIAS=upload SIGNING_KEY_PASSWORD=password ./gradlew :app:bundleRelease

// `./gradlew :app:generateReleaseDatabaseSchema` to create the sqldelight database
// `./gradlew :app:verifySqlDelightMigration` to verify the migration
