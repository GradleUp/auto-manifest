plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdkVersion(29)

    defaultConfig {
        applicationId = "com.gradleup.auto.manifest.sample"
        minSdkVersion(21)
    }
}

dependencies {
    implementation(project(":feature:ui"))
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.core:core-ktx:1.2.0")
}
