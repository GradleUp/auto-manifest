package com.gradleup.auto.manifest.feature

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.gradleup.auto.manifest.sample.feature.FeatureRepository
import com.gradleup.auto.manifest.sample.feature.ui.R

class FeatureFragment : Fragment(R.layout.feature_fragment) {

    private val repository = FeatureRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        repository.load()
    }
}
