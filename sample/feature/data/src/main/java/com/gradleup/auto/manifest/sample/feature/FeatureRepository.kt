package com.gradleup.auto.manifest.sample.feature

import com.gradleup.auto.manifest.sample.sharedFun

class FeatureRepository {

    fun load() : List<String> {
        sharedFun()
        return listOf("foo", "bar")
    }
}
