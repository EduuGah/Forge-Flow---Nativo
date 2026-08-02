package com.forgeflow.app

import android.app.Application
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.forgeflow.app.icon.LauncherIconManager
import com.forgeflow.app.navigation.AppShortcutInitializer
import com.forgeflow.app.widget.ForgeFlowWidgetCoordinator
import com.forgeflow.app.widget.NutritionWidgetCoordinator
import com.forgeflow.core.data.exercise.ExerciseSeedInitializer
import com.forgeflow.core.data.backup.PendingLocalDataRestore
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

    @Inject
    lateinit var appShortcutInitializer: AppShortcutInitializer

    @Inject
    lateinit var widgetCoordinator: ForgeFlowWidgetCoordinator

    @Inject
    lateinit var nutritionWidgetCoordinator: NutritionWidgetCoordinator

    override fun attachBaseContext(base: Context) {
        PendingLocalDataRestore.applyIfPending(base)
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        notificationChannelInitializer.initialize()
        exerciseSeedInitializer.initialize()
        appShortcutInitializer.initialize()
        widgetCoordinator.start()
        nutritionWidgetCoordinator.start()
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStop(owner: LifecycleOwner) {
                    launcherIconManager.applyScheduledUpdate()
                }
            },
        )
    }
}
