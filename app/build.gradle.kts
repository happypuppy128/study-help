plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.xuexiengine"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.xuexiengine"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
}
