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
    private lateinit var runner: GradleRunner

    fun build(taskName: String, extensionBlock: String = ""): BuildResult {
        buildFile.appendText(extensionBlock)
        return runner.withArguments(taskName, "--stacktrace").build()
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
        File(projectDir, "settings.gradle").createNewFile()
        buildFile.writeText(
            """
                buildscript {
                    repositories {
                        google()
                        mavenCentral()
                        jcenter()
                    }
                    dependencies {
                        classpath("com.android.tools.build:gradle:3.6.3")
                    }
                }
                plugins {
                    id 'com.gradleup.auto.manifest'
                    id 'com.android.library'
                }
                
                allprojects {
                    repositories {
                        google()
                        mavenCentral()
                        jcenter()
                    }
                }
                
                android {
                    compileSdkVersion 28
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
