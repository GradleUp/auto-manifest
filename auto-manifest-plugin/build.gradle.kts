plugins {
    id("java-gradle-plugin")
    kotlin("jvm") version "1.3.72"
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "0.11.0"
    `maven-publish`
}

gradlePlugin {
    plugins {
        create("autoManifest") {
            id = "com.gradleup.auto.manifest"
            displayName = "Auto Manifest Gradle Plugin"
            description = "Automatically creates AndroidManifest.xml so you don't have to"
            implementationClass = "com.gradleup.auto.manifest.AutoManifestPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/GradleUp/auto-manifest/"
    vcsUrl = "https://github.com/GradleUp/auto-manifest.git"
    tags = listOf("android", "AndroidManifest", "GradleUp")
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            groupId = findProperty("GROUP")?.toString()
            version = findProperty("VERSION_NAME")?.toString()

            name.set(findProperty("POM_NAME")?.toString())
            description.set(findProperty("POM_DESCRIPTION")?.toString())
            packaging = findProperty("POM_PACKAGING")?.toString()
            url.set(findProperty("POM_URL")?.toString())

            scm {
                url.set(findProperty("POM_SCM_URL")?.toString())
                connection.set(findProperty("POM_SCM_CONNECTION")?.toString())
            }

            licenses {
                license {
                    name.set(findProperty("POM_LICENCE_NAME")?.toString())
                    url.set(findProperty("POM_LICENCE_URL")?.toString())
                }
            }
        }
    }

    repositories {
        maven {
            name = "bintray"
            url = uri("https://api.bintray.com/maven/gradleup/maven/auto-manifest/;publish=1;override=1")
            credentials {
                username = System.getenv("BINTRAY_USER")
                password = System.getenv("BINTRAY_API_KEY")
            }
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.android.tools.build:gradle:3.6.3")

    testImplementation("junit:junit:4.13")
    testImplementation("com.google.truth:truth:1.0.1")
}
