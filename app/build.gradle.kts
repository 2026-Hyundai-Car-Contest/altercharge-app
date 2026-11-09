import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.altercharge.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.altercharge.app"
        minSdk = 28 // Pleos Connect 요구사항: API 28 이상
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // 서명은 keystore.properties가 있을 때만 적용한다. (키스토어는 저장소에 올리지 않는다)
            val keystoreFile = rootProject.file("keystore.properties")
            if (keystoreFile.exists()) {
                val props = Properties().apply { keystoreFile.inputStream().use { load(it) } }
                signingConfig = signingConfigs.create("release") {
                    storeFile = rootProject.file(props.getProperty("storeFile"))
                    storePassword = props.getProperty("storePassword")
                    keyAlias = props.getProperty("keyAlias")
                    keyPassword = props.getProperty("keyPassword")
                }
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)

    // Pleos Connect SDK
    implementation(libs.pleos.vehicle)
}
