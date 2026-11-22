import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "bg.rado.molitvenik"
    compileSdk = 35

    base {
        archivesName.set("molitvenik")
    }

    defaultConfig {
        applicationId = "bg.rado.molitvenik"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"
    }

    // Ключът за подписване се подава отвън (GitHub секрети или keystore.properties) — никога не е в репото.
    val keystoreProps = Properties().apply {
        val f = rootProject.file("keystore.properties")
        if (f.exists()) f.inputStream().use { load(it) }
    }
    fun secret(env: String, prop: String): String? = System.getenv(env) ?: keystoreProps.getProperty(prop)
    val storePath = secret("KEYSTORE_PATH", "storeFile")

    signingConfigs {
        if (storePath != null && file(storePath).length() > 0 && secret("KEYSTORE_PASSWORD", "storePassword") != null) {
            create("release") {
                storeFile = file(storePath)
                storePassword = secret("KEYSTORE_PASSWORD", "storePassword")
                keyAlias = secret("KEY_ALIAS", "keyAlias")
                keyPassword = secret("KEY_PASSWORD", "keyPassword")
            }
        }
    }

    buildTypes {
        release {
            // Без ключ (напр. липсващи секрети) се подписва с debug ключа, за да може APK-то пак да се инсталира.
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
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
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
