package com.gradleup.auto.manifest

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.Test
import java.io.File

class NestedModuleSupportTests {

    @Rule
    @JvmField
    val testProject = TestProjectRule()

    private val generatedManifest = File(testProject.projectDir, GENERATED_MANIFEST_PATH)

    @Test
    fun `should recursively generate manifest`() {
        val libraries = listOf("library1", "library1/data", "library2")
        testProject.createNestedModules(libraries)

        val result = testProject.build("assembleDebug", "autoManifest { packageName = 'test' }")

        assertThat(result.task(":assembleDebug")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(generatedManifest.readText()).contains("<manifest package=\"test\" />")

        libraries.forEach {
            val libraryDir = File(testProject.projectDir, it)
            val libraryManifest = File(libraryDir, Companion.GENERATED_MANIFEST_PATH)
            val suffix = it.replace('/', '.')
            assertThat(libraryManifest.readText()).contains("<manifest package=\"test.$suffix\" />")
        }
    }

    @Test
    fun `should NOT recursively generate manifest when applyRecursively = false`() {
        testProject.createNestedModules(listOf("library"))

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
    fun `when disabled only for a module, should skip generating manifest`() {
        testProject.createNestedModules(listOf("library1", "library2"))

        // When: set the property to disable only for one module
        val leafDir = File(testProject.projectDir, "library2")
        File(leafDir, "build.gradle").appendText("""
            apply plugin: 'com.gradleup.auto.manifest'
            
            autoManifest.disable()
        """.trimIndent())

        // expected to fail because of lack of AndroidManifest file
        val result = testProject.buildAndFail("assembleDebug", "autoManifest { packageName = 'test' }")

        assertThat(result.task(":assembleDebug")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(generatedManifest.readText()).contains("<manifest package=\"test\" />")

        val enabledLibraryManifest = File(testProject.projectDir, "library1/$GENERATED_MANIFEST_PATH")
        assertThat(enabledLibraryManifest.exists()).isTrue()

        val disabledLibraryManifest = File(testProject.projectDir, "library2/$GENERATED_MANIFEST_PATH")
        assertThat(disabledLibraryManifest.exists()).isFalse()
        assertThat(result.task(":library2:generateAndroidManifest")!!.outcome).isEqualTo(TaskOutcome.SKIPPED)
    }

    @Test
    fun `should override packageName in when applied in leaf module`() {
        val leafModule = "library1/data"
        val leafDir = File(testProject.projectDir, leafModule)
        val packageNameToOverride = "com.test.override"
        testProject.createNestedModules(listOf(leafModule))

        File(leafDir, "build.gradle").appendText("""
            apply plugin: 'com.gradleup.auto.manifest'
            
            autoManifest { packageName = '$packageNameToOverride' }
        """.trimIndent())

        testProject.build("assembleDebug", "autoManifest { packageName = 'test' }")

        val libraryManifest = File(leafDir, GENERATED_MANIFEST_PATH)
        assertThat(libraryManifest.readText()).contains("<manifest package=\"$packageNameToOverride\" />")
    }

    companion object {
        private const val GENERATED_MANIFEST_PATH = "build/generated/auto-manifest/AndroidManifest.xml"
    }

}

