package com.gradleup.auto.manifest

import java.io.File

fun TestProjectRule.createNestedModules(moduleNames: List<String>) {
    val commaSeparatedModules = moduleNames.joinToString(separator = ",") {
        "'${it.replace('/', ':')}'"
    }
    File(projectDir, "settings.gradle").appendText(
        "include($commaSeparatedModules)"
    )
    moduleNames.forEach {
        val moduleDir = File(projectDir, it)
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

