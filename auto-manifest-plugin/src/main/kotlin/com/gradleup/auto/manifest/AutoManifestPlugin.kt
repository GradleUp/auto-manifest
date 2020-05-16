package com.gradleup.auto.manifest

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

@Suppress("unused")
class AutoManifestPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create<AutoManifestExtension>("autoManifest")
        AutoManifestConfigurator(target, extension).configure()
    }
}
