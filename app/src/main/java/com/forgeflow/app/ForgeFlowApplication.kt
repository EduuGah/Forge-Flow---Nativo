package com.forgeflow.app

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.forgeflow.app.icon.LauncherIconManager
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

    @Inject
    lateinit var launcherIconManager: LauncherIconManager

    override fun onCreate() {
        super.onCreate()
        notificationChannelInitializer.initialize()
        exerciseSeedInitializer.initialize()
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStop(owner: LifecycleOwner) {
                    launcherIconManager.applyScheduledUpdate()
                }
            },
        )
    }
}
