package com.gradleup.auto.manifest

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.AndroidSourceFile
import com.android.build.gradle.api.AndroidSourceSet
import com.android.build.gradle.tasks.GenerateBuildConfig
import com.android.build.gradle.tasks.ManifestProcessorTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.closureOf
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import java.io.File

interface AutoManifestExtension {
    val packageName: Property<String>

    val generatedManifest: RegularFileProperty
}

@Suppress("unused")
class AutoManifestPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create<AutoManifestExtension>("autoManifest")
        target.setup(extension)
    }

    private fun Project.setup(extension: AutoManifestExtension) {
        plugins.withType<LibraryPlugin> {
            extensions.configure<BaseExtension>("android") {
                sourceSets.getByName("main") {
                    if (manifest.srcFile.isFile) {
                        logger.warn("AndroidManifest.xml already exists. Skipping auto generation.")
                    } else {
                        setupGeneratedManifest(this, extension)
                    }
                }
            }
        }
    }

    private fun Project.setupGeneratedManifest(sourceSet: AndroidSourceSet, extension: AutoManifestExtension) {
        sourceSet.manifest(sourceSet.closureOf<AndroidSourceFile> {
            srcFile(File(buildDir, GenerateManifestTask.GENERATED_MANIFEST_PATH))
        })

        val generateManifest = tasks.register<GenerateManifestTask>("generateAndroidManifest") {
            packageName.set(extension.packageName)
            extension.generatedManifest.apply {
                set(manifestFile)
                finalizeValue()
            }
        }
        tasks.withType<GenerateBuildConfig>().configureEach {
            mustRunAfter(generateManifest)
        }
        tasks.withType<ManifestProcessorTask>().configureEach {
            dependsOn(generateManifest)
        }
    }
}
