plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = 31
    defaultConfig {
        minSdk = 21
    }
    resourcePrefix("feature")
}

dependencies {
    implementation(project(":feature:data"))
    implementation(project(":base-theme"))
    implementation("androidx.fragment:fragment:1.4.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
}
