package com.forgeflow.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.net.toUri
import com.forgeflow.app.R
import com.forgeflow.app.navigation.AppLaunchDestination
import com.forgeflow.app.navigation.forgeFlowLaunchIntent
import com.forgeflow.core.common.di.ApplicationScope
import com.forgeflow.core.data.nutrition.NutritionRepository
import com.forgeflow.core.data.settings.SettingsRepository
import com.forgeflow.core.model.AccentColor
import com.forgeflow.core.model.HydrationEntry
import com.forgeflow.core.model.NutritionJournal
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Singleton
class NutritionWidgetCoordinator @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val nutritionRepository: NutritionRepository,
    private val settingsRepository: SettingsRepository,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
) {
    private val isObserving = AtomicBoolean(false)

    fun start() {
        if (nutritionWidgetIds().isEmpty() && hydrationWidgetIds().isEmpty()) return
        if (!isObserving.compareAndSet(false, true)) return
        applicationScope.launch {
            observeState()
                .distinctUntilChanged()
                .collect { state ->
                    updateNutritionWidgets(nutritionWidgetIds(), state)
                    updateHydrationWidgets(hydrationWidgetIds(), state)
                }
        }
    }

    fun refreshNutrition(widgetIds: IntArray? = null) {
        val targetIds = widgetIds ?: nutritionWidgetIds()
        if (targetIds.isEmpty()) return
        applicationScope.launch { updateNutritionWidgets(targetIds, observeState().first()) }
    }

    fun refreshHydration(widgetIds: IntArray? = null) {
        val targetIds = widgetIds ?: hydrationWidgetIds()
        if (targetIds.isEmpty()) return
        applicationScope.launch { updateHydrationWidgets(targetIds, observeState().first()) }
    }

    fun addWater(milliliters: Int = WATER_QUICK_ADD_MILLILITERS) {
        applicationScope.launch {
            nutritionRepository.addHydration(
                HydrationEntry(
                    id = UUID.randomUUID().toString(),
                    milliliters = milliliters,
                    consumedAt = Instant.now(),
                ),
            )
        }
    }

    private fun observeState() = combine(
        nutritionRepository.observeJournal(),
        settingsRepository.observeSettings(),
    ) { journal, settings ->
        journal.toWidgetState(settings.accentColor)
    }

    private fun NutritionJournal.toWidgetState(accentColor: AccentColor): NutritionWidgetState {
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()
        val mealsToday = meals.filter { it.eatenAt.atZone(zone).toLocalDate() == today }
        val hydrationToday = hydration.filter {
            it.consumedAt.atZone(zone).toLocalDate() == today
        }
        return NutritionWidgetState(
            calories = mealsToday.sumOf { it.calories },
            calorieGoal = goals.calories.coerceAtLeast(1),
            proteinGrams = mealsToday.sumOf { it.proteinGrams }.roundToInt(),
            carbohydrateGrams = mealsToday.sumOf { it.carbohydrateGrams }.roundToInt(),
            fatGrams = mealsToday.sumOf { it.fatGrams }.roundToInt(),
            waterMilliliters = hydrationToday.sumOf { it.milliliters },
            waterGoalMilliliters = goals.waterMilliliters.coerceAtLeast(1),
            accentColor = accentColor,
        )
    }

    private fun updateNutritionWidgets(widgetIds: IntArray, state: NutritionWidgetState) {
        if (widgetIds.isEmpty()) return
        val manager = AppWidgetManager.getInstance(context)
        widgetIds.forEach { widgetId ->
            val views = RemoteViews(context.packageName, R.layout.widget_nutrition)
            val accent = state.accentColor.toArgb()
            val progress = state.calories.progressTo(state.calorieGoal)
            val isCompact = manager.getAppWidgetOptions(widgetId)
                .getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT) in 1..109

            views.setTextColor(R.id.nutrition_widget_brand, accent)
            views.setTextColor(R.id.nutrition_widget_action, accent)
            views.setTextViewText(
                R.id.nutrition_widget_calories,
                context.getString(
                    R.string.nutrition_widget_calories,
                    state.calories,
                    state.calorieGoal,
                ),
            )
            views.setTextViewText(
                R.id.nutrition_widget_macros,
                context.getString(
                    R.string.nutrition_widget_macros,
                    state.proteinGrams,
                    state.carbohydrateGrams,
                    state.fatGrams,
                ),
            )
            views.setTextViewText(
                R.id.nutrition_widget_water,
                context.getString(
                    R.string.nutrition_widget_water,
                    state.waterMilliliters,
                    state.waterGoalMilliliters,
                ),
            )
            views.setProgressBar(R.id.nutrition_widget_progress, PROGRESS_MAX, progress, false)
            views.setViewVisibility(
                R.id.nutrition_widget_macros,
                if (isCompact) View.GONE else View.VISIBLE,
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                views.setColorStateList(
                    R.id.nutrition_widget_progress,
                    "setProgressTintList",
                    ColorStateList.valueOf(accent),
                )
            }
            val launchIntent = launchPendingIntent(widgetId, "nutrition")
            views.setOnClickPendingIntent(R.id.nutrition_widget_content, launchIntent)
            views.setOnClickPendingIntent(R.id.nutrition_widget_action, launchIntent)
            manager.updateAppWidget(widgetId, views)
        }
    }

    private fun updateHydrationWidgets(widgetIds: IntArray, state: NutritionWidgetState) {
        if (widgetIds.isEmpty()) return
        val manager = AppWidgetManager.getInstance(context)
        widgetIds.forEach { widgetId ->
            val views = RemoteViews(context.packageName, R.layout.widget_hydration)
            val accent = state.accentColor.toArgb()
            views.setTextColor(R.id.hydration_widget_brand, accent)
            views.setTextColor(R.id.hydration_widget_add, accent)
            views.setTextViewText(
                R.id.hydration_widget_value,
                context.getString(
                    R.string.hydration_widget_value,
                    state.waterMilliliters,
                    state.waterGoalMilliliters,
                ),
            )
            views.setProgressBar(
                R.id.hydration_widget_progress,
                PROGRESS_MAX,
                state.waterMilliliters.progressTo(state.waterGoalMilliliters),
                false,
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                views.setColorStateList(
                    R.id.hydration_widget_progress,
                    "setProgressTintList",
                    ColorStateList.valueOf(accent),
                )
            }
            views.setOnClickPendingIntent(
                R.id.hydration_widget_content,
                launchPendingIntent(widgetId, "hydration"),
            )
            views.setOnClickPendingIntent(
                R.id.hydration_widget_add,
                addWaterPendingIntent(widgetId),
            )
            manager.updateAppWidget(widgetId, views)
        }
    }

    private fun launchPendingIntent(widgetId: Int, source: String): PendingIntent {
        val intent = context.forgeFlowLaunchIntent(AppLaunchDestination.NUTRITION).apply {
            data = "forgeflow://widget/$source/$widgetId".toUri()
        }
        return PendingIntent.getActivity(
            context,
            widgetId * REQUEST_CODE_MULTIPLIER + source.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun addWaterPendingIntent(widgetId: Int): PendingIntent {
        val intent = Intent(context, HydrationWidgetProvider::class.java).apply {
            action = HydrationWidgetProvider.ACTION_ADD_WATER
            data = "forgeflow://widget/hydration/$widgetId/add-water".toUri()
        }
        return PendingIntent.getBroadcast(
            context,
            widgetId * REQUEST_CODE_MULTIPLIER + ADD_WATER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun nutritionWidgetIds(): IntArray = AppWidgetManager.getInstance(context)
        .getAppWidgetIds(ComponentName(context, NutritionWidgetProvider::class.java))

    private fun hydrationWidgetIds(): IntArray = AppWidgetManager.getInstance(context)
        .getAppWidgetIds(ComponentName(context, HydrationWidgetProvider::class.java))

    private fun Int.progressTo(goal: Int): Int =
        (toFloat() / goal.coerceAtLeast(1) * PROGRESS_MAX).roundToInt()
            .coerceIn(0, PROGRESS_MAX)

    private fun AccentColor.toArgb(): Int = when (this) {
        AccentColor.BLUE -> 0xFF2F80ED.toInt()
        AccentColor.CYAN -> 0xFF00A7C4.toInt()
        AccentColor.TEAL -> 0xFF009688.toInt()
        AccentColor.GREEN -> 0xFF2EAD64.toInt()
        AccentColor.LIME -> 0xFF7FAF22.toInt()
        AccentColor.AMBER -> 0xFFD49200.toInt()
        AccentColor.ORANGE -> 0xFFEA6C21.toInt()
        AccentColor.RED -> 0xFFE74C4C.toInt()
        AccentColor.ROSE -> 0xFFE94D72.toInt()
        AccentColor.PINK -> 0xFFE84B9B.toInt()
        AccentColor.PURPLE -> 0xFF9A65E5.toInt()
        AccentColor.INDIGO -> 0xFF626ED4.toInt()
    }

    private companion object {
        const val PROGRESS_MAX = 100
        const val WATER_QUICK_ADD_MILLILITERS = 250
        const val REQUEST_CODE_MULTIPLIER = 100
        const val ADD_WATER_REQUEST_CODE = 71
    }
}

private data class NutritionWidgetState(
    val calories: Int,
    val calorieGoal: Int,
    val proteinGrams: Int,
    val carbohydrateGrams: Int,
    val fatGrams: Int,
    val waterMilliliters: Int,
    val waterGoalMilliliters: Int,
    val accentColor: AccentColor,
)
