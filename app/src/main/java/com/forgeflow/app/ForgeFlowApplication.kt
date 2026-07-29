package com.forgeflow.app

import android.app.Application
import com.forgeflow.core.data.exercise.ExerciseSeedInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ForgeFlowApplication : Application() {
    @Inject
    lateinit var exerciseSeedInitializer: ExerciseSeedInitializer

    override fun onCreate() {
        super.onCreate()
        exerciseSeedInitializer.initialize()
    }
}
