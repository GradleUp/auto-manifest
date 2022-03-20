package com.gradleup.auto.manifest

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
abstract class GenerateManifestTask : DefaultTask() {
    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val replaceDashesWithDot: Property<Boolean>

    @get:Nested
    abstract val applicationSettings: Property<AutoManifestExtension.ApplicationSettings>

    @get:Input
    abstract val projectPath: Property<String>

    @get:Input
    abstract val rootProjectPath: Property<String>

    @get:OutputFile
    abstract val manifestFile: RegularFileProperty

    init {
        @Suppress("LeakingThis")
        manifestFile.set(File(project.buildDir, GENERATED_MANIFEST_PATH))
    }

    @TaskAction
    fun taskAction() {
        manifestFile.get().asFile.apply {
            val suffix =
                pathSuffixFor(rootProjectPath.get(), projectPath.get(), replaceDashesWithDot)
            generateManifest(this, suffix, packageName.orNull, applicationSettings.get())
        }
    }

    companion object {
        const val GENERATED_MANIFEST_PATH = "generated/auto-manifest/AndroidManifest.xml"

        fun generateManifest(
            manifestFile: File,
            suffix: String,
            packageName: String?,
            application: AutoManifestExtension.ApplicationSettings
        ) {
            requireNotNull(packageName) {
                "Please provide packageName in your build.gradle file. E.g: autoManifest { packageName = \"com.company.package\" }"
            }
            manifestFile.parentFile.mkdirs()
            val generateApplicationTag = GenerateApplicationTag(application)
            val applicationTag = generateApplicationTag()
            if (applicationTag.isEmpty()) {
                manifestFile.writeText(
                    """
                    <?xml version="1.0" encoding="utf-8"?>
                    <manifest package="$packageName${suffix.prependDot()}" />
                    """.trimIndent()
                )
            } else {
                manifestFile.writeText(
                    """|<?xml version="1.0" encoding="utf-8"?>
                    |<manifest xmlns:android="http://schemas.android.com/apk/res/android"
                    |  package="$packageName${suffix.prependDot()}">
                    |  $applicationTag
                    |</manifest>
                """.trimMargin()
                )
            }
        }

        fun pathSuffixFor(
            rootProjectPath: String,
            currentProjectPath: String,
            replaceDashesWithDot: Provider<Boolean>
        ): String {
            return currentProjectPath
                .removePrefix(rootProjectPath)
                .removePrefix(":")
                .replace(':', '.')
                .replace("-", replaceDashesWithDot.map { if (it) "." else "_" }.get())
        }

        private fun String.prependDot() = if (isNotEmpty()) ".$this" else this
    }
}
