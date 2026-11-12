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

    // 백엔드 API 주소. 값은 gradle.properties(altercharge.*BaseUrl)에 있고, 앱 코드는 BuildConfig.BASE_URL로 읽는다.
    val debugBaseUrl = providers.gradleProperty("altercharge.debugBaseUrl").getOrElse("http://10.0.2.2:8080/api/v1/")
    val releaseBaseUrl = providers.gradleProperty("altercharge.releaseBaseUrl").getOrElse("")
    // 출시 주소는 HTTPS만 (Pleos 앱 보안 가이드, 출시 빌드는 평문 통신이 막혀 있다). 비어 있으면 아직 배포 전이다.
    if (releaseBaseUrl.isNotEmpty() && !releaseBaseUrl.startsWith("https://")) {
        throw GradleException("altercharge.releaseBaseUrl은 https:// 주소여야 해요: $releaseBaseUrl")
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"$debugBaseUrl\"")
        }
        release {
            buildConfigField("String", "BASE_URL", "\"$releaseBaseUrl\"")
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
        buildConfig = true // BuildConfig.BASE_URL
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
    testImplementation(libs.org.json) // 서버 응답 JSON 테스트용. APK에는 안 들어간다

    // Pleos Connect SDK
    implementation(libs.pleos.vehicle)
}
