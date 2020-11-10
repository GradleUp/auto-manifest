plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdkVersion(29)
    defaultConfig {
        minSdkVersion(21)
    }
    resourcePrefix("feature")
}

dependencies {
    implementation(project(":feature:data"))
    implementation("androidx.fragment:fragment:1.2.5")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
}
