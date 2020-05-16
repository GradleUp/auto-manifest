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

    private fun createNestedModules(moduleNames: List<String>) {
        val commaSeparatedModules = moduleNames.joinToString(separator = ",") {
            "'${it.replace('/', ':')}'"
        }
        File(testProject.projectDir, "settings.gradle").writeText(
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
                    android { compileSdkVersion 28 }
                """.trimIndent()
            )
        }
    }
}

