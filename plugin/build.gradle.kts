import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-gradle-plugin")
    kotlin("jvm") version "1.3.72"
}

gradlePlugin {
    plugins {
        create("auto-manifest") {
            id = "com.gradleup.auto.manifest"
            implementationClass = "com.gradleup.auto.manifest.AutoManifestPlugin"
        }
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.android.tools.build:gradle:3.6.3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
