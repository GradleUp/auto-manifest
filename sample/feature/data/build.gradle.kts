plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdkVersion(29)
}

dependencies {
    implementation(kotlin("stdlib"))
    api(project(":library"))
}
