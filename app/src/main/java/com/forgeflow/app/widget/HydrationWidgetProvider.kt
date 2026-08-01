package com.forgeflow.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HydrationWidgetProvider : AppWidgetProvider() {
    @Inject
    lateinit var coordinator: NutritionWidgetCoordinator

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_ADD_WATER) {
            coordinator.addWater()
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        coordinator.start()
        coordinator.refreshHydration()
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        coordinator.start()
        coordinator.refreshHydration(appWidgetIds)
    }

    companion object {
        const val ACTION_ADD_WATER = "com.forgeflow.app.widget.ADD_WATER"
    }
}
