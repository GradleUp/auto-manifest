package com.gradleup.auto.manifest

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class AutoManifestDuringSyncTests {

    @Rule
    @JvmField
    val testProject = TestProjectRule()

    private val generatedManifest = testProject.generatedFile("auto-manifest/AndroidManifest.xml")

    @Test
    fun `should generate manifest during Android Studio sync`() {
        testProject.build(
            taskName = "help",
            extensionBlock = "autoManifest { packageName = 'test' }",
            arg = "-Pandroid.injected.invoked.from.ide=true"
        )

        assertThat(generatedManifest.readText()).contains("<manifest/>")
    }

    @Test
    fun `should NOT generate manifest during configuration`() {
        testProject.build(
            taskName = "help",
            extensionBlock = "autoManifest { packageName = 'test' }",
        )

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

}

