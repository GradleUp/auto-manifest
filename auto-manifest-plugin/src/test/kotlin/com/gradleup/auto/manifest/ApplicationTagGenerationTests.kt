package com.gradleup.auto.manifest

import com.google.common.truth.Truth
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.Test

class ApplicationTagGenerationTests {

    @Rule
    @JvmField
    val testProject = TestProjectRule()

    @Test
    fun `should generate all application properties`() {
        val result = testProject.build(
            "assembleDebug",
            """
                autoManifest { 
                    packageName = 'test'
                    
                    application {
                        supportsRtl = true
                        theme = "@style/Theme.RoadRunner"
                    }
                }
            """.trimIndent()
        )

        Truth.assertThat(result.task(":assembleDebug")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        @Language("xml")
        val expected = """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
              package="test">
              <application
                android:theme="@style/Theme.RoadRunner"
                android:supportsRtl="true"
              />
            </manifest>
        """.trimIndent()
        Truth.assertThat(testProject.generatedFile().readText()).isEqualTo(expected)
    }

    @Test
    fun `should generate single property`() {
        val result = testProject.build(
            "assembleDebug",
            """
                autoManifest { 
                    packageName = 'test'
                    application.supportsRtl = true
                }
            """.trimIndent()
        )

        Truth.assertThat(result.task(":assembleDebug")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        @Language("xml")
        val expected = """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
              package="test">
              <application
                android:supportsRtl="true"
              />
            </manifest>
        """.trimIndent()
        Truth.assertThat(testProject.generatedFile().readText()).isEqualTo(expected)
    }
}
