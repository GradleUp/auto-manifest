package com.gradleup.auto.manifest

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
abstract class GenerateManifestTask : DefaultTask() {
    @get:Input abstract val packageName: Property<String>

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:OutputFile val manifestFile: RegularFileProperty = project.objects.fileProperty().apply {
        set(File(project.buildDir, GENERATED_MANIFEST_PATH))
    }

    @TaskAction
    fun taskAction() {
        manifestFile.get().asFile.apply {
            parentFile.mkdirs()
            val packageName = requireNotNull(packageName.getOrNull()) {
                "Please provide packageName in your build.gradle file. Ex: autoManifest { packageName = \"com.company.package\""
            }
            writeText(
                """
                    <?xml version="1.0" encoding="utf-8"?>
                    <manifest package="$packageName" />
                """.trimIndent()
            )
        }
    }

    companion object {
        const val GENERATED_MANIFEST_PATH = "generated/auto-manifest/AndroidManifest.xml"
    }
}
