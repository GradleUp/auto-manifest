package com.gradleup.auto.manifest

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import java.io.File

class AutoManifestDuringSyncTests {

    @Rule
    @JvmField
    val testProject = TestProjectRule()

    private val generatedManifest = File(testProject.projectDir, GENERATED_MANIFEST_PATH)

    @Test
    fun `should generate manifest during Android Studio sync`() {
        testProject.build(
            taskName = "help",
            extensionBlock = "autoManifest { packageName = 'test' }",
            arg = "-Pandroid.injected.invoked.from.ide=true"
        )

        assertThat(generatedManifest.readText()).contains("<manifest package=\"test\" />")
    }

    @Test
    fun `should NOT generate manifest during configuration`() {
        testProject.build("help")

        assertThat(generatedManifest.exists()).isFalse()
    }

    @Test
    fun `given disabled, should NOT generate manifest during Android Studio sync`() {
        testProject.build(
            taskName = "help",
            extensionBlock = "autoManifest { disable() }",
            arg = "-Pandroid.injected.invoked.from.ide=true"
        )
        assertThat(generatedManifest.exists()).isFalse()
    }

    companion object {
        private const val GENERATED_MANIFEST_PATH =
            "build/generated/auto-manifest/AndroidManifest.xml"
    }

}

