package com.gradleup.auto.manifest

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.AndroidSourceFile
import com.android.build.gradle.api.AndroidSourceSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.closureOf
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.withType
import java.io.File

interface AutoManifestExtension {
    val packageName: Property<String>
}

@Suppress("unused")
class AutoManifestPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create<AutoManifestExtension>("autoManifest")
        val manifestFile = File(target.buildDir, "generated/auto-manifest/AndroidManifest.xml")

        target.plugins.withType<LibraryPlugin> {
            val android = target.extensions.getByName<BaseExtension>("android")

            android.sourceSets.getByName("main") {
                if (manifest.srcFile.isFile) {
                    target.logger.warn("AndroidManifest.xml already exists. Skipping auto generation.")
                } else {
                    setupGeneratedManifest(manifestFile, target, extension)
                }
            }
        }
    }

    private fun AndroidSourceSet.setupGeneratedManifest(
        manifestFile: File,
        target: Project,
        extension: AutoManifestExtension
    ) {
        manifest(closureOf<AndroidSourceFile> {
            srcFile(manifestFile)
        })

        target.afterEvaluate {
            manifestFile.parentFile.mkdirs()
            manifestFile.writeText(
                """
                    <?xml version="1.0" encoding="utf-8"?>
                    <manifest package="${extension.packageName.get()}" />
                """.trimIndent()
            )
        }
    }
}
