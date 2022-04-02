plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "com.gradleup.auto.manifest.sample"
        minSdk = 21
    }

    lint {
      checkDependencies = true
    }
}

dependencies {
    implementation(project(":feature:ui"))
    implementation(project(":base-theme"))
    implementation("androidx.core:core-ktx:1.7.0")
}
