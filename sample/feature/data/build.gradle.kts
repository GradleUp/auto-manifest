plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = 31
}

dependencies {
    api(project(":library"))
}
