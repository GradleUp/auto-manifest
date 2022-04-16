package com.gradleup.auto.manifest

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
abstract class GenerateManifestTask : DefaultTask() {

    @get:OutputFile
    abstract val manifestFile: RegularFileProperty

    init {
        @Suppress("LeakingThis")
        manifestFile.set(File(project.buildDir, GENERATED_MANIFEST_PATH))
    }

    @TaskAction
    fun taskAction() {
        manifestFile.get().asFile.apply {
            generateManifest(this)
        }
    }

    companion object {
        const val GENERATED_MANIFEST_PATH = "generated/auto-manifest/AndroidManifest.xml"

        fun generateManifest(manifestFile: File) {
            manifestFile.parentFile.mkdirs()
            manifestFile.writeText(
                """
                    <?xml version="1.0" encoding="utf-8"?>
                    <manifest/>
                """.trimIndent()
            )
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
    }
}
