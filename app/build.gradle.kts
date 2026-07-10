plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.nhockool1002.costoftrips"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nhockool1002.costoftrips"
        minSdk = 24
        targetSdk = 35
        versionCode = 5
        versionName = "1.0.0.a3"

        // The app only ships English (default) + Vietnamese strings, but
        // AndroidX libraries (AppCompat, Compose, etc.) bundle their own
        // string resources translated into ~70 locales. Without this, every
        // one of those ships in the APK/AAB even though the app UI never
        // surfaces them.
        resourceConfigurations += listOf("en", "vi")
    }

    val keystorePath = System.getenv("KEYSTORE_PATH")
    val keystorePassword = System.getenv("KEYSTORE_PASSWORD")
    val keyAlias = System.getenv("KEY_ALIAS")
    val keyPassword = System.getenv("KEY_PASSWORD")
    val hasReleaseSigningEnv = listOf(keystorePath, keystorePassword, keyAlias, keyPassword).all { !it.isNullOrBlank() }

    signingConfigs {
        if (hasReleaseSigningEnv) {
            create("release") {
                storeFile = file(keystorePath!!)
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (hasReleaseSigningEnv) {
                signingConfig = signingConfigs.getByName("release")
            }
            // Bundles native debug symbols (from Room/SQLite's native libs) into the
            // AAB automatically, so Play Console stops warning about missing symbols
            // instead of requiring a manual upload.
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    // material-icons-extended was removed: every icon the app actually uses
    // (Add, Delete, Settings, AutoMirrored ArrowBack/KeyboardArrowRight) ships
    // in material-icons-core, which material3 already pulls in transitively.
    // Verified via a clean release build that this makes no measurable size
    // difference either way (R8 was already shrinking the extended set down
    // to the same handful of icons) — dropped mainly to shrink the dependency
    // graph, not for size.

    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("sh.calvin.reorderable:reorderable:2.4.3")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.datastore:datastore-preferences:1.1.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
