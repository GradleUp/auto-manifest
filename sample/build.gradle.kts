buildscript {
    repositories {
        google()
        jcenter()

    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.10")
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
        jcenter()
    }
}
