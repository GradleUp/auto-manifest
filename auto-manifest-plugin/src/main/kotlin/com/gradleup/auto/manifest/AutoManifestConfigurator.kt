package com.gradleup.auto.manifest

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.AndroidSourceFile
import com.android.build.gradle.api.AndroidSourceSet
import com.android.build.gradle.tasks.GenerateBuildConfig
import com.android.build.gradle.tasks.ManifestProcessorTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.closureOf
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import java.io.File

internal class AutoManifestConfigurator(
    private val rootProject: Project,
    private val extension: AutoManifestExtension
) {

    fun configure() {
        rootProject.configure()
        extension.generatedManifest.apply {
            set(rootProject.manifestFile)
            finalizeValue()
        }
        rootProject.afterEvaluate {
            if (extension.applyRecursively.getOrElse(true)) {
                subprojects.forEach { project ->
                    project.configure()
                }
            }
        }
    }

    private fun Project.configure() = plugins.withType<LibraryPlugin> {
        extensions.configure<LibraryExtension>("android") {
            sourceSets.getByName("main") {
                if (manifest.srcFile.isFile) {
                    logger.info("AutoManifest: AndroidManifest.xml already exists. Skipping auto generation.")
                } else {
                    setupGeneratedManifest(project = this@configure)
                }
            }
        }
    }

    private fun AndroidSourceSet.setupGeneratedManifest(project: Project) {
        manifest(closureOf<AndroidSourceFile> {
            srcFile(project.manifestFile)
        })

        if (project.isSyncing()) {
            project.forceGenerateDuringSync()
        }

        val generateManifest = project.tasks.register<GenerateManifestTask>("generateAndroidManifest") {
            packageName.set(extension.packageName)
            projectPath.set(project.path)
            rootProjectPath.set(rootProject.path)
        }
        project.tasks.withType<GenerateBuildConfig>().configureEach {
            mustRunAfter(generateManifest)
        }
        project.tasks.withType<ManifestProcessorTask>().configureEach {
            dependsOn(generateManifest)
        }
    }

    private fun Project.forceGenerateDuringSync() {
        val packageName = extension.packageName
        val suffix = GenerateManifestTask.pathSuffixFor(
            rootProjectPath = rootProject.path,
            currentProjectPath = path
        )
        if (manifestFile.exists().not()) {
            if (packageName.isPresent) {
                GenerateManifestTask.generateManifest(
                    manifestFile,
                    packageName,
                    suffix
                )
            } else {
                afterEvaluate {
                    GenerateManifestTask.generateManifest(
                        manifestFile,
                        packageName,
                        suffix
                    )
                }
            }
        }
    }

    private fun Project.isSyncing() = hasProperty("android.injected.invoked.from.ide")

    private val Project.manifestFile get() = File(
        buildDir,
        GenerateManifestTask.GENERATED_MANIFEST_PATH
    )
}
