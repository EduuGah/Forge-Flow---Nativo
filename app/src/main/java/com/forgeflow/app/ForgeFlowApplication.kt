package com.forgeflow.app

import android.app.Application
import com.forgeflow.core.data.exercise.ExerciseSeedInitializer
import com.forgeflow.core.platform.notification.NotificationChannelInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ForgeFlowApplication : Application() {
    @Inject
    lateinit var exerciseSeedInitializer: ExerciseSeedInitializer

    @Inject
    lateinit var notificationChannelInitializer: NotificationChannelInitializer

    override fun onCreate() {
        super.onCreate()
        notificationChannelInitializer.initialize()
        exerciseSeedInitializer.initialize()
    }
}
