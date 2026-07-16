plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("jacoco")
}

android {
    namespace = "com.nhockool1002.costoftrips"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nhockool1002.costoftrips"
        minSdk = 24
        targetSdk = 35
        versionCode = 7
        versionName = "1.0.0.a5"

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
        debug {
            enableUnitTestCoverage = true
        }
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
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

    implementation("androidx.work:work-runtime-ktx:2.9.1")

    debugImplementation("androidx.compose.ui:ui-tooling")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    // Android's built-in org.json classes are stubs on the host JVM; this pulls
    // in a real implementation so DataExporter's JSON logic can be unit-tested
    // without Robolectric.
    testImplementation("org.json:json:20240303")
    testImplementation("org.robolectric:robolectric:4.13")
    testImplementation("androidx.test:core-ktx:1.6.1")
    testImplementation("androidx.test.ext:junit:1.2.1")
    testImplementation("androidx.work:work-testing:2.9.1")
}

// JVM unit-test coverage, scoped to the testable business logic (data layer,
// util, notification scheduling, ViewModels) rather than Composable UI code,
// which isn't meaningfully exercised by JUnit/Robolectric unit tests.
tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    group = "verification"
    description = "Generates a JaCoCo HTML/XML coverage report for the debug unit tests."

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val coverageExcludes = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "android/**/*.*",
        "**/MainActivity.*",
        "**/CostOfTripsApp.*",
        "**/ui/theme/**",
        "**/ui/navigation/AppBottomBar*",
        "**/ui/navigation/CostOfTripsNavHost*",
        "**/ui/screens/about/**",
        "**/ui/screens/splash/**",
        "**/ui/screens/common/**",
        "**/ui/screens/**/*Screen.*",
        "**/*\$Composable*.*",
        "**/ComposableSingletons\$*.*"
    )

    val javaClasses = fileTree("$buildDir/intermediates/javac/debug/classes") { exclude(coverageExcludes) }
    val kotlinClasses = fileTree("$buildDir/tmp/kotlin-classes/debug") { exclude(coverageExcludes) }
    classDirectories.setFrom(files(javaClasses, kotlinClasses))
    sourceDirectories.setFrom(files("$projectDir/src/main/java"))
    executionData.setFrom(
        fileTree(buildDir) {
            include(
                "outputs/unit_test_code_coverage/debugUnitTest/*.exec",
                "jacoco/testDebugUnitTest.exec"
            )
        }
    )
}
