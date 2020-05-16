buildscript {
    repositories {
        google()
        jcenter()

    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.6.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72")
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
