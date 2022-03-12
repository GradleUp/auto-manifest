subprojects {
    group = property("GROUP")!!
    version = property("VERSION_NAME")!!

    repositories {
        google()
        mavenCentral()
    }
}
