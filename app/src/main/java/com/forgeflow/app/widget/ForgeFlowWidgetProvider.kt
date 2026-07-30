package com.forgeflow.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ForgeFlowWidgetProvider : AppWidgetProvider() {
    @Inject
    lateinit var coordinator: ForgeFlowWidgetCoordinator

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        coordinator.start()
        coordinator.refresh()
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        coordinator.start()
        coordinator.refresh(appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle,
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        coordinator.refresh(intArrayOf(appWidgetId))
    }
}
