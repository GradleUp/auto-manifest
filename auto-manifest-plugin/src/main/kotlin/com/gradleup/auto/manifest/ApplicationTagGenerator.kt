package com.gradleup.auto.manifest

import org.gradle.api.provider.Provider

class GenerateApplicationTag(
    private val applicationSettings: AutoManifestExtension.ApplicationSettings
) {

    operator fun invoke(): String {
        val internal = buildInternal()
        return when {
            internal.isEmpty() -> ""
            else -> "<application\n${internal.joinToString("\n")}\n  />"
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun buildInternal() = buildList {
        applicationSettings.name.ifPresent {
            add("    android:name=\"$it\"")
        }
        applicationSettings.label.ifPresent {
            add("    android:label=\"$it\"")
        }
        applicationSettings.icon.ifPresent {
            add("    android:icon=\"$it\"")
        }
        applicationSettings.roundIcon.ifPresent {
            add("    android:roundIcon=\"$it\"")
        }
        applicationSettings.theme.ifPresent {
            add("    android:theme=\"$it\"")
        }
        applicationSettings.supportsRtl.ifPresent {
            add("    android:supportsRtl=\"$it\"")
        }
    }

    private fun <T> Provider<T>.ifPresent(action: (T) -> Unit) {
        if (isPresent) {
            action.invoke(get())
        }
    }

}
