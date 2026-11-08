plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.realsalmon.studentfit"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.realsalmon.studentfit"
        // Health Connect(4단계 워치 연동)가 Android 8.0(API 26) 이상을 요구해요.
        minSdk = 26
        targetSdk = 35
        // CI에서는 실행 번호를 버전 코드로 써서 새 APK가 항상 더 높은 버전이 돼요.
        versionCode = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1
        versionName = "0.2.0"
    }

    signingConfigs {
        // 저장소에 고정된 디버그 키를 써서, 새 빌드를 받아도 지우지 않고 덮어 설치할 수 있게 해요.
        // (개인 테스트용 키예요. 플레이스토어 배포 땐 따로 비밀 키를 만들어야 해요.)
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.mlkit.pose)
    implementation(libs.androidx.health.connect)
    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
}
