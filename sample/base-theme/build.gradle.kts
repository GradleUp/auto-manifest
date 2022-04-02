plugins {
    id("com.android.library")
}

android {
    compileSdk = 31
    defaultConfig {
        minSdk = 21
    }
}

dependencies {
    api("androidx.appcompat:appcompat:1.4.1")
}
