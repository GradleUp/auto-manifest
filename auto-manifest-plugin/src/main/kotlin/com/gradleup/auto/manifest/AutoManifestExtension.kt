package com.gradleup.auto.manifest

import org.gradle.api.Action
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

abstract class AutoManifestExtension @Inject constructor(
    objects: ObjectFactory
) {

    val application = objects.newInstance<ApplicationSettings>()

    fun application(action: Action<ApplicationSettings>) {
        action.execute(application)
    }

    /**
     * packageName that will be set in generated AndroidManifest.xml
     *
     * Android Gradle Plugin will use this to generate BuildConfig and R classes under this package.
     */
    abstract val packageName: Property<String>

    /**
     * Applies recursively to all modules (subprojects), so you don't have to configure every single module.
     * Submodule package names will be auto generated by using their relative path
     *
     * Default: true
     */
    abstract val applyRecursively: Property<Boolean>

    /**
     * Disables auto manifest generation per module (aka subproject)
     *
     * When [applyRecursively] is enabled, if you face any issues on a certain module with custom
     * setup, you can use this to disable for that module.
     *
     * Example in a module:
     *
     * plugins {
     *   id("com.android.library")
     *   id("auto-manifest")
     * }
     *
     * autoManifest.disable()
     */
    fun disable() {
        enabled.set(false)
    }

    /**
     * Use [disable] function to disable auto manifest generation
     *
     * @see AutoManifestExtension.disable
     */
    abstract val enabled: Property<Boolean>

    /**
     * Using dashes `-` is pretty common in module names. But they are not allowed within Java package names.
     *
     * When this flag is enabled, they will be replaced by a dot. By default, they will be replaced with an underscore.
     * Default is set to `false` to prevent package name collusion in rare cases.
     *
     * Default: false
     */
    abstract val replaceDashesWithDot: Property<Boolean>

    /**
     * Read-only generated AndroidManifest.xml location for advanced users.
     */
    abstract val generatedManifest: RegularFileProperty


    abstract class ApplicationSettings {
        @get:Input @get:Optional abstract val name: Property<String>
        @get:Input @get:Optional abstract val label: Property<String>
        @get:Input @get:Optional abstract val icon: Property<String>
        @get:Input @get:Optional abstract val roundIcon: Property<String>
        @get:Input @get:Optional abstract val supportsRtl: Property<Boolean>
        @get:Input @get:Optional abstract val theme: Property<String>
    }
}
