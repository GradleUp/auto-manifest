package com.gradleup.auto.manifest

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

interface AutoManifestExtension {
    val packageName: Property<String>
    val applyRecursively: Property<Boolean>

    val generatedManifest: RegularFileProperty
}
