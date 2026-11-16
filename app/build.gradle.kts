import java.util.Properties
import java.io.FileInputStream

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

    val envVersionName = System.getenv("VERSION_NAME") ?: project.findProperty("versionName") as? String
    val envVersionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: (project.findProperty("versionCode") as? String)?.toIntOrNull()

    defaultConfig {
        applicationId = "bg.rado.molitvenik"
        minSdk = 26
        targetSdk = 35
        versionCode = envVersionCode ?: 1
        versionName = envVersionName ?: "1.0"
    }

    signingConfigs {
        create("release") {
            val keystorePathEnv = System.getenv("KEYSTORE_PATH")
            val keystorePasswordEnv = System.getenv("KEYSTORE_PASSWORD")
            val keyAliasEnv = System.getenv("KEY_ALIAS")
            val keyPasswordEnv = System.getenv("KEY_PASSWORD")

            val props = Properties()
            val propsFile = rootProject.file("keystore.properties")
            if (propsFile.exists()) {
                props.load(FileInputStream(propsFile))
            }

            val storePath = keystorePathEnv ?: props.getProperty("storeFile")
            val storePass = keystorePasswordEnv ?: props.getProperty("storePassword")
            val aliasVal = keyAliasEnv ?: props.getProperty("keyAlias")
            val keyPass = keyPasswordEnv ?: props.getProperty("keyPassword")

            if (!storePath.isNullOrBlank()) {
                val f = file(storePath)
                val rf = rootProject.file(storePath)
                if (f.exists()) {
                    storeFile = f
                } else if (rf.exists()) {
                    storeFile = rf
                }
            }
            storePassword = storePass
            keyAlias = aliasVal
            keyPassword = keyPass
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            val releaseSigning = signingConfigs.findByName("release")
            if (releaseSigning?.storeFile != null && releaseSigning.storeFile!!.exists()) {
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
