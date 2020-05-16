package com.gradleup.auto.manifest

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
abstract class GenerateManifestTask : DefaultTask() {
    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val replaceDashesWithDot: Property<Boolean>

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
            val suffix = pathSuffixFor(rootProjectPath.get(), projectPath.get(), replaceDashesWithDot)
            generateManifest(this, packageName, suffix)
        }
    }

    companion object {
        const val GENERATED_MANIFEST_PATH = "generated/auto-manifest/AndroidManifest.xml"

        fun generateManifest(
            manifestFile: File,
            packageNameProperty: Property<String>,
            suffix: String
        ) {
            val packageName = requireNotNull(packageNameProperty.getOrNull()) {
                "Please provide packageName in your build.gradle file. E.g: autoManifest { packageName = \"com.company.package\" }"
            }
            manifestFile.parentFile.mkdirs()
            manifestFile.writeText(
                """
                    <?xml version="1.0" encoding="utf-8"?>
                    <manifest package="$packageName${suffix.prependDot()}" />
                """.trimIndent()
            )
        }

        fun pathSuffixFor(
            rootProjectPath: String,
            currentProjectPath: String,
            replaceDashesWithDot: Property<Boolean>
        ): String {
            return currentProjectPath
                .removePrefix(rootProjectPath)
                .replace(':', '.')
                .replace("-", replaceDashesWithDot.map { if (it) "." else "_" }.get())
        }

        private fun String.prependDot() = if (isNotEmpty()) ".$this" else this
    }
}
