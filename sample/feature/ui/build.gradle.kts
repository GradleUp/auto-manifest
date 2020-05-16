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
    implementation(kotlin("stdlib"))
    implementation(project(":feature:data"))
    implementation("androidx.fragment:fragment:1.2.4")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
}
