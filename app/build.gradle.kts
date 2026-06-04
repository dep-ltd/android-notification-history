import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

fun loadDotEnv(): Map<String, String> {
    val envFile = rootProject.file(".env")
    if (!envFile.exists()) return emptyMap()
    return envFile.readLines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
        .associate { line ->
            val idx = line.indexOf('=')
            line.substring(0, idx).trim() to line.substring(idx + 1).trim()
        }
}

fun envOrLocalProperty(key: String): String? {
    loadDotEnv()[key]?.takeIf { it.isNotEmpty() }?.let { return it }
    val localProps = rootProject.file("local.properties")
    if (!localProps.exists()) return null
    val props = Properties().apply { localProps.inputStream().use { load(it) } }
    return props.getProperty(key)?.takeIf { it.isNotEmpty() }
}

android {
    namespace = "com.notificationhistory"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.notificationhistory"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "DEBUG_DB_ENCRYPTION_KEY", "\"\"")
        buildConfigField("boolean", "ALLOW_ROOT_FOR_DEBUG", "false")
    }

    signingConfigs {
        create("release") {
            val storeFilePath = envOrLocalProperty("PLAY_UPLOAD_STORE_FILE")
            val storePassword = envOrLocalProperty("PLAY_UPLOAD_STORE_PASSWORD")
            val keyAlias = envOrLocalProperty("PLAY_UPLOAD_KEY_ALIAS")
            val keyPassword = envOrLocalProperty("PLAY_UPLOAD_KEY_PASSWORD")
            if (storeFilePath != null && storePassword != null && keyAlias != null && keyPassword != null) {
                val keystoreFile = rootProject.file(storeFilePath)
                if (keystoreFile.exists()) {
                    storeFile = keystoreFile
                    this.storePassword = storePassword
                    this.keyAlias = keyAlias
                    this.keyPassword = keyPassword
                }
            }
        }
    }

    buildTypes {
        debug {
            val debugDbKey = envOrLocalProperty("DEBUG_DB_ENCRYPTION_KEY") ?: ""
            buildConfigField("String", "DEBUG_DB_ENCRYPTION_KEY", "\"$debugDbKey\"")
            buildConfigField("boolean", "ALLOW_ROOT_FOR_DEBUG", "true")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null) {
                signingConfig = releaseSigning
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
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.kotlinx.coroutines.android)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Security (phase 3 wiring; dependency declared in phase 0)
    implementation(libs.sqlcipher)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.biometric)

    // Navigation & Work
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.coil.compose)
    implementation(libs.androidx.work.runtime.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
