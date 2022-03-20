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
                        name = ".SampleApplication"
                        label = "@string/app_name"
                        icon = "@mipmap/ic_launcher"
                        roundIcon = "@mipmap/ic_launcher_round"
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
                android:name=".SampleApplication"
                android:label="@string/app_name"
                android:icon="@mipmap/ic_launcher"
                android:roundIcon="@mipmap/ic_launcher_round"
                android:theme="@style/Theme.RoadRunner"
                android:supportsRtl="true"
              />
            </manifest>
        """.trimIndent()
        Truth.assertThat(testProject.generatedFile().readText()).isEqualTo(expected)
    }
}
