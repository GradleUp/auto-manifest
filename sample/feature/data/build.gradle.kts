plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdkVersion(29)
}

dependencies {
    api(project(":library"))
}
