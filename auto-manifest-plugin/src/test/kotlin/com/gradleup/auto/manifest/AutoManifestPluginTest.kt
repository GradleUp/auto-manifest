package com.gradleup.auto.manifest

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.Test
import java.io.File

class AutoManifestPluginTest {

    @Rule
    @JvmField
    val testProject = TestProjectRule()

    private val regularManifest =
        File(testProject.projectDir, "src/main/AndroidManifest.xml")
    private val generatedManifest =
        File(testProject.projectDir, "build/generated/auto-manifest/AndroidManifest.xml")

    @Test
    fun `should fail without plugin and manifest`() {
        testProject.buildAndFail("assembleDebug")
    }

    @Test
    fun `should be successful when plugin is applied`() {
        val result = testProject.build("assembleDebug", "autoManifest { packageName = 'test' }")

        assertThat(result.task(":assembleDebug")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(generatedManifest.readText()).contains("<manifest package=\"test\" />")
    }

    @Test
    fun `given manifest exists, should skip generation`() {
        regularManifest.parentFile.mkdirs()
        regularManifest.writeText(
            """
                <?xml version="1.0" encoding="utf-8"?>
                <manifest package="com.gradleup.example" />
            """.trimIndent()
        )
        val result = testProject.build("assembleDebug")

        assertThat(result.task(":assembleDebug")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":generateAndroidManifest")).isNull()
    }

    @Test
    fun `should recursively generate manifest`() {
        val libraries = listOf("library1", "library1/data", "library2")
        createNestedModules(libraries)

        val result = testProject.build("assembleDebug", "autoManifest { packageName = 'test' }")

        assertThat(result.task(":assembleDebug")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(generatedManifest.readText()).contains("<manifest package=\"test\" />")

        libraries.forEach {
            val libraryDir = File(testProject.projectDir, it)
            val libraryManifest = File(libraryDir, "build/generated/auto-manifest/AndroidManifest.xml")
            val suffix = it.replace('/', '.')
            assertThat(libraryManifest.readText()).contains("<manifest package=\"test.$suffix\" />")
        }
    }

    @Test
    fun `should NOT recursively generate manifest when applyRecursively = false`() {
        createNestedModules(listOf("library"))

        // fails because Manifest for lib is not available
        val result = testProject.buildAndFail(
            taskName = "assembleDebug",
            extensionBlock = """
                autoManifest { 
                    packageName = 'test'
                    applyRecursively = false
                }
            """.trimIndent()
        )

        assertThat(result.output).contains("Execution failed for task ':library:generateDebugBuildConfig'")
        assertThat(result.output).contains("> Manifest file does not exist:")
        assertThat(result.task(":library:generateAndroidManifest")).isNull()
    }

    @Test
    fun `should generate manifest when module name has dash in them`() {
        createNestedModules(listOf("library-dash"))

        testProject.build("assembleDebug", "autoManifest { packageName = 'test' }")

        val libraryDir = File(testProject.projectDir, "library-dash")
        val libraryManifest = File(libraryDir, "build/generated/auto-manifest/AndroidManifest.xml")
        assertThat(libraryManifest.readText()).contains("<manifest package=\"test.library_dash\" />")
    }

    @Test
    fun `should generate manifest with dot when module name has dash in them AND replaceDashesWithDot is true`() {
        createNestedModules(listOf("library-dash"))

        testProject.build(
            taskName = "assembleDebug",
            extensionBlock = """
                autoManifest { 
                    packageName = 'test'
                    replaceDashesWithDot = true
                }
            """.trimIndent()
        )

        val libraryDir = File(testProject.projectDir, "library-dash")
        val libraryManifest = File(libraryDir, "build/generated/auto-manifest/AndroidManifest.xml")
        assertThat(libraryManifest.readText()).contains("<manifest package=\"test.library.dash\" />")
    }

    @Test
    fun `should override packageName in when applied in leaf module`() {
        val leafModule = "library1/data"
        val leafDir = File(testProject.projectDir, leafModule)
        val packageNameToOverride = "com.test.override"
        createNestedModules(listOf(leafModule))

        File(leafDir, "build.gradle").appendText("""
            
            apply plugin: 'com.gradleup.auto.manifest'
            
            autoManifest { packageName = '$packageNameToOverride' }
        """.trimIndent())

        testProject.build("assembleDebug", "autoManifest { packageName = 'test' }")

        val libraryManifest = File(leafDir, "build/generated/auto-manifest/AndroidManifest.xml")
        assertThat(libraryManifest.readText()).contains("<manifest package=\"$packageNameToOverride\" />")
    }

    private fun createNestedModules(moduleNames: List<String>) {
        val commaSeparatedModules = moduleNames.joinToString(separator = ",") {
            "'${it.replace('/', ':')}'"
        }
        File(testProject.projectDir, "settings.gradle").appendText(
            "include($commaSeparatedModules)"
        )
        moduleNames.forEach {
            val moduleDir = File(testProject.projectDir, it)
            moduleDir.mkdirs()
            File(moduleDir, "build.gradle").writeText(
                """
                    plugins {
                        id 'com.android.library'
                    }
                    android { 
                        compileSdkVersion 31 
                    }
                """.trimIndent()
            )
        }
    }
}

