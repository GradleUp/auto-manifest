buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.1.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.20-RC")
    }
}
plugins {
    id("com.gradleup.auto.manifest")
}

autoManifest {
    packageName.set("com.gradleup.auto.manifest.sample")
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
