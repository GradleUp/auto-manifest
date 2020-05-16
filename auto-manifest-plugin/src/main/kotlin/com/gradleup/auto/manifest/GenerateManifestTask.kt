package com.gradleup.auto.manifest

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.hasPlugin
import java.io.File

@CacheableTask
abstract class GenerateManifestTask : DefaultTask() {
    @get:Input
    abstract val packageName: Property<String>

    @get:OutputFile
    val manifestFile: RegularFileProperty = project.objects.fileProperty()

    init {
        manifestFile.set(File(project.buildDir, GENERATED_MANIFEST_PATH))
    }

    @TaskAction
    fun taskAction() {
        logger.error(project.path)
        logger.error(manifestFile.get().asFile.path)
        manifestFile.get().asFile.apply {
            parentFile.mkdirs()
            val packageName = requireNotNull(packageName.getOrNull()) {
                "Please provide packageName in your build.gradle file. Ex: autoManifest { packageName = \"com.company.package\""
            }
            val suffix = project.path
                .removePrefix(project.findRootProject().path)
                .replace(':', '.')
            writeText(
                """
                    <?xml version="1.0" encoding="utf-8"?>
                    <manifest package="$packageName${suffix.prependDot()}" />
                """.trimIndent()
            )
        }
    }

    private tailrec fun Project.findRootProject(): Project = when {
        plugins.hasPlugin(AutoManifestPlugin::class) -> this
        else -> parent!!.findRootProject()
    }

    private fun String.prependDot() = if (isNotEmpty()) ".$this" else this

    companion object {
        const val GENERATED_MANIFEST_PATH = "generated/auto-manifest/AndroidManifest.xml"
    }
}
