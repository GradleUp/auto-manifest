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
        regularManifest.writeText("""
            <?xml version="1.0" encoding="utf-8"?>
            <manifest package="com.gradleup.example" />
        """.trimIndent())
        val result = testProject.build("assembleDebug")

        assertThat(result.task(":assembleDebug")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":generateAndroidManifest")).isNull()
    }
}

