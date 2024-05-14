plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "kr.co.pyflu.sociallogin"
    compileSdk = 34

    defaultConfig {
        applicationId = "kr.co.pyflu.sociallogin"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    dataBinding{
        enable = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.fragment:fragment-ktx:1.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    implementation("androidx.core:core-splashscreen:1.0.1") // SplashScreen

    implementation(platform("com.google.firebase:firebase-bom:33.0.0")) // firebase
    implementation("com.google.firebase:firebase-analytics-ktx:22.0.0")
    implementation("com.google.firebase:firebase-auth-ktx:23.0.0")
    implementation("com.google.firebase:firebase-firestore-ktx:25.0.0")

    implementation("androidx.credentials:credentials:1.2.2") // 구글 로그인
    implementation("androidx.credentials:credentials-play-services-auth:1.2.2")

    implementation("com.kakao.sdk:v2-user:2.20.1") // 카카오 로그인

    implementation("com.navercorp.nid:oauth:5.9.1") // 네이버 로그인
}