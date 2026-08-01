package com.forgeflow.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NutritionWidgetProvider : AppWidgetProvider() {
    @Inject
    lateinit var coordinator: NutritionWidgetCoordinator

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        coordinator.start()
        coordinator.refreshNutrition()
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        coordinator.start()
        coordinator.refreshNutrition(appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle,
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        coordinator.refreshNutrition(intArrayOf(appWidgetId))
    }
}
