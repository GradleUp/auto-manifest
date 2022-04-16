package com.gradleup.auto.manifest

import com.android.build.api.dsl.AndroidSourceSet
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.tasks.GenerateBuildConfig
import com.android.build.gradle.tasks.ManifestProcessorTask
import com.android.build.gradle.tasks.MergeResources
import com.gradleup.auto.manifest.GenerateManifestTask.Companion.generateManifest
import com.gradleup.auto.manifest.GenerateManifestTask.Companion.pathSuffixFor
import org.gradle.api.Project
import org.gradle.api.provider.Provider
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
                subprojects.configureSubjects()
            }
        }
    }

    private fun Set<Project>.configureSubjects() = forEach {
        it.afterEvaluate {
            if (pluginManager.hasPlugin("com.gradleup.auto.manifest").not()) {
                configure()
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
        manifest {
            srcFile(project.manifestFile)
        }

        if (project.isSyncing()) {
            project.forceGenerateDuringSync()
        }

        val generateManifest = project.registerGenerateManifest()
        project.tasks.withType<GenerateBuildConfig>().configureEach {
            mustRunAfter(generateManifest)
        }
        project.tasks.withType<MergeResources>().configureEach {
            dependsOn(generateManifest)
        }
        project.tasks.withType<ManifestProcessorTask>().configureEach {
            dependsOn(generateManifest)
        }
    }

    private fun Project.forceGenerateDuringSync() {
        if (manifestFile.exists()) return

        executeOnceAvailable(extension.packageName) { packageName ->
            if (extension.enabled.getOrElse(true).not()) return@executeOnceAvailable
            val suffix = pathSuffixFor(
                rootProjectPath = rootProject.path,
                currentProjectPath = path,
                replaceDashesWithDot = extension.replaceDashesWithDot.orElse(false)
            )
            generateManifest(manifestFile, suffix, packageName)
        }
    }

    private fun Project.registerGenerateManifest() =
        tasks.register<GenerateManifestTask>("generateAndroidManifest") {
            /**
             * Re-assigning to a local variable to keep configuration cache.
             * Otherwise inner lambda (onlyIf) will accidentally reference to `Project` instance
             */
            val extension = extension
            onlyIf {
                extension.enabled.getOrElse(true)
            }
            packageName.set(extension.packageName)
            replaceDashesWithDot.set(extension.replaceDashesWithDot.orElse(false))
            projectPath.set(this@registerGenerateManifest.path)
            rootProjectPath.set(this@AutoManifestConfigurator.rootProject.path)
        }

    private fun <T : Any> Project.executeOnceAvailable(provider: Provider<T>, block: (T?) -> Unit) {
        if (provider.isPresent) {
            block(provider.orNull)
        } else {
            afterEvaluate { block(provider.orNull) }
        }
    }

    companion object {

        private val Project.manifestFile
            get() = File(buildDir, GenerateManifestTask.GENERATED_MANIFEST_PATH)

        private fun Project.isSyncing() = hasProperty("android.injected.invoked.from.ide")
    }
}
