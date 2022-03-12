package com.gradleup.auto.manifest

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.io.File

class TestProjectRule : TestRule {

    val projectDir = File(
        System.getProperty("user.dir"),
        "build/test-projects/${System.currentTimeMillis()}"
    )
    private val buildFile = File(projectDir, "build.gradle")
    private val settingsFile = File(projectDir, "settings.gradle")
    private lateinit var runner: GradleRunner

    fun build(taskName: String, extensionBlock: String = "", arg: String? = null): BuildResult {
        buildFile.appendText(extensionBlock)
        return runner.apply {
            withArguments(taskName, "--stacktrace")
            arg?.let { withArguments(arg) }
        }.build()
    }

    fun buildAndFail(taskName: String, extensionBlock: String = ""): BuildResult {
        buildFile.appendText(extensionBlock)
        return runner.withArguments(taskName, "--stacktrace").buildAndFail()
    }

    override fun apply(base: Statement, description: Description?) = object : Statement() {
        override fun evaluate() {
            runner = createRunner()
            base.evaluate()
            projectDir.deleteRecursively()
        }
    }

    private fun createRunner(): GradleRunner {
        projectDir.mkdirs()
        settingsFile.writeText(
            """
                pluginManagement {
                    repositories {
                        gradlePluginPortal()
                        google()
                    }
                }
                
            """.trimIndent()
        )
        buildFile.writeText(
            """
                plugins {
                    id("com.gradleup.auto.manifest")
                    id("com.android.library") version "7.1.2"
                }
                
                allprojects {
                    repositories {
                        google()
                        mavenCentral()
                    }
                }
                
                android {
                    compileSdkVersion(31)
                }
                
            """.trimIndent()
        )
        val valuesDir = File(projectDir, "src/main/res/values")
        valuesDir.mkdirs()
        File(valuesDir, "strings.xml").writeText(
            """
                <resources>
                  <string name="app_name">AutoManifest</string>
                </resources>
            """.trimIndent()
        )

        return GradleRunner.create()
            .forwardStdOutput(System.out.writer())
            .forwardStdError(System.err.writer())
            .withProjectDir(projectDir)
            .withPluginClasspath()
    }

}
