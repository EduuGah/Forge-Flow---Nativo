package com.forgeflow.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import androidx.core.net.toUri
import com.forgeflow.app.R
import com.forgeflow.app.navigation.AppLaunchDestination
import com.forgeflow.app.navigation.forgeFlowLaunchIntent
import com.forgeflow.core.common.di.ApplicationScope
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.settings.SettingsRepository
import com.forgeflow.core.data.workout.WorkoutRepository
import com.forgeflow.core.model.AccentColor
import com.forgeflow.core.model.UserSettings
import com.forgeflow.core.model.WorkoutDetails
import com.forgeflow.core.model.calculateWorkoutStreakStats
import com.forgeflow.core.model.nextScheduledWorkoutDate
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.NumberFormat
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Singleton
class ForgeFlowWidgetCoordinator @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val workoutRepository: WorkoutRepository,
    private val settingsRepository: SettingsRepository,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
) {
    private val isObserving = AtomicBoolean(false)

    fun start() {
        if (allWidgetIds().isEmpty()) return
        if (!isObserving.compareAndSet(false, true)) return
        applicationScope.launch {
            observeState()
                .distinctUntilChanged()
                .collect(::updateAllWidgets)
        }
    }

    fun refresh(widgetIds: IntArray? = null) {
        val targetWidgetIds = widgetIds ?: allWidgetIds()
        if (targetWidgetIds.isEmpty()) return
        applicationScope.launch {
            updateWidgets(
                widgetIds = targetWidgetIds,
                state = observeState().first(),
            )
        }
    }

    private fun observeState() = combine(
        workoutRepository.observeHistory(),
        workoutRepository.observeActiveWorkout(),
        settingsRepository.observeSettings(),
    ) { historyResult, activeResult, settings ->
        createWidgetState(
            history = (historyResult as? DataResult.Success)?.value.orEmpty(),
            activeWorkout = (activeResult as? DataResult.Success)?.value,
            settings = settings,
        )
    }

    private fun createWidgetState(
        history: List<WorkoutDetails>,
        activeWorkout: WorkoutDetails?,
        settings: UserSettings,
    ): ForgeFlowWidgetState {
        val today = LocalDate.now()
        val workoutDates = history.mapTo(mutableSetOf()) { workout ->
            workout.session.startedAt.atZone(ZoneId.systemDefault()).toLocalDate()
        }
        val streak = calculateWorkoutStreakStats(workoutDates, today)
        val nextWorkout = nextScheduledWorkoutDate(
            today = today,
            trainingDays = settings.trainingDays,
            completedDates = workoutDates,
        )
        val latestWorkout = history.maxByOrNull { it.session.startedAt }
        return ForgeFlowWidgetState(
            weeklyWorkoutCount = streak.currentWeekCount,
            weeklyGoal = settings.weeklyWorkoutGoal.coerceAtLeast(1),
            currentStreak = streak.current,
            nextWorkoutLabel = nextWorkout?.toWidgetDate(
                today = today,
                timeMinutes = settings.preferredWorkoutTimeMinutes,
            ),
            activeWorkoutName = activeWorkout?.session?.name,
            activeCompletedSets = activeWorkout?.completedSetCount ?: 0,
            activeTotalSets = activeWorkout?.totalSetCount ?: 0,
            activeExerciseCount = activeWorkout?.exercises?.size ?: 0,
            activeStartedAtEpochMillis = activeWorkout?.session?.startedAt?.toEpochMilli(),
            latestWorkoutName = latestWorkout?.session?.name,
            latestWorkoutDurationMinutes = latestWorkout?.let { workout ->
                Duration.between(
                    workout.session.startedAt,
                    workout.session.finishedAt ?: workout.session.startedAt,
                ).toMinutes().coerceAtLeast(0)
            },
            latestWorkoutVolume = latestWorkout?.totalVolumeGrams
                ?.div(settings.weightUnit.gramsPerDisplayUnit),
            weightUnitSymbol = settings.weightUnit.symbol,
            accentColor = settings.accentColor,
        )
    }

    private fun updateAllWidgets(state: ForgeFlowWidgetState) {
        updateWidgets(allWidgetIds(), state)
    }

    private fun updateWidgets(
        widgetIds: IntArray,
        state: ForgeFlowWidgetState,
    ) {
        if (widgetIds.isEmpty()) return
        val manager = AppWidgetManager.getInstance(context)
        widgetIds.forEach { widgetId ->
            manager.updateAppWidget(
                widgetId,
                createRemoteViews(
                    state = state,
                    widgetId = widgetId,
                    options = manager.getAppWidgetOptions(widgetId),
                ),
            )
        }
    }

    private fun createRemoteViews(
        state: ForgeFlowWidgetState,
        widgetId: Int,
        options: android.os.Bundle,
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_forgeflow)
        val isActive = state.activeWorkoutName != null
        val maxHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
        val maxWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
        val isCompact = maxHeight in 1..149
        val isNarrow = maxWidth in 1..279
        val accent = state.accentColor.toArgb()
        val progress = (
            state.weeklyWorkoutCount.toFloat() / state.weeklyGoal * WIDGET_PROGRESS_MAX
            ).toInt().coerceIn(0, WIDGET_PROGRESS_MAX)

        views.setTextViewText(
            R.id.widget_week_progress,
            context.getString(
                R.string.widget_week_progress,
                state.weeklyWorkoutCount,
                state.weeklyGoal,
            ),
        )
        views.setProgressBar(
            R.id.widget_progress,
            WIDGET_PROGRESS_MAX,
            progress,
            false,
        )
        views.setTextColor(R.id.widget_primary_action, accent)
        views.setInt(R.id.widget_history_action, "setColorFilter", accent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            views.setColorStateList(
                R.id.widget_progress,
                "setProgressTintList",
                android.content.res.ColorStateList.valueOf(accent),
            )
        }

        if (isActive) {
            views.setTextViewText(R.id.widget_eyebrow, context.getString(R.string.widget_active))
            views.setTextViewText(R.id.widget_title, state.activeWorkoutName)
            views.setTextViewText(
                R.id.widget_supporting,
                context.resources.getQuantityString(
                    R.plurals.widget_active_summary,
                    state.activeExerciseCount,
                    state.activeCompletedSets,
                    state.activeTotalSets,
                    state.activeExerciseCount,
                ),
            )
            views.setTextViewText(
                R.id.widget_primary_action,
                context.getString(R.string.widget_continue),
            )
            state.activeStartedAtEpochMillis?.let { startedAt ->
                val elapsed = (System.currentTimeMillis() - startedAt).coerceAtLeast(0)
                views.setChronometer(
                    R.id.widget_timer,
                    SystemClock.elapsedRealtime() - elapsed,
                    "%s",
                    true,
                )
                views.setViewVisibility(R.id.widget_timer, View.VISIBLE)
            }
        } else {
            views.setTextViewText(
                R.id.widget_eyebrow,
                context.getString(R.string.widget_next_workout),
            )
            views.setTextViewText(
                R.id.widget_title,
                state.nextWorkoutLabel ?: context.getString(R.string.widget_ready),
            )
            views.setTextViewText(
                R.id.widget_supporting,
                state.latestWorkoutName?.let { latest ->
                    context.getString(
                        R.string.widget_latest_workout_details,
                        latest,
                        state.latestWorkoutDurationMinutes.toWidgetDuration(),
                        state.latestWorkoutVolume.toWidgetVolume(),
                        state.weightUnitSymbol,
                    )
                } ?: context.getString(R.string.widget_first_workout),
            )
            views.setTextViewText(
                R.id.widget_primary_action,
                context.getString(R.string.widget_train),
            )
            views.setViewVisibility(R.id.widget_timer, View.GONE)
        }

        views.setTextViewText(
            R.id.widget_streak,
            context.resources.getQuantityString(
                R.plurals.widget_streak,
                state.currentStreak,
                state.currentStreak,
            ),
        )
        views.setViewVisibility(
            R.id.widget_supporting,
            if (isCompact) View.GONE else View.VISIBLE,
        )
        views.setViewVisibility(
            R.id.widget_streak,
            if (isCompact || isNarrow) View.GONE else View.VISIBLE,
        )
        views.setViewVisibility(
            R.id.widget_history_action,
            if (isNarrow) View.GONE else View.VISIBLE,
        )

        val primaryDestination = if (isActive) {
            AppLaunchDestination.ACTIVE_WORKOUT
        } else {
            AppLaunchDestination.ROUTINES
        }
        views.setOnClickPendingIntent(
            R.id.widget_primary_action,
            launchPendingIntent(widgetId, primaryDestination),
        )
        views.setOnClickPendingIntent(
            R.id.widget_history_action,
            launchPendingIntent(widgetId, AppLaunchDestination.HISTORY),
        )
        views.setOnClickPendingIntent(
            R.id.widget_content,
            launchPendingIntent(
                widgetId,
                if (isActive) AppLaunchDestination.ACTIVE_WORKOUT else AppLaunchDestination.HOME,
            ),
        )
        return views
    }

    private fun launchPendingIntent(
        widgetId: Int,
        destination: AppLaunchDestination,
    ): PendingIntent {
        val intent = context.forgeFlowLaunchIntent(destination).apply {
            data = "forgeflow://widget/$widgetId/${destination.value}".toUri()
        }
        return PendingIntent.getActivity(
            context,
            widgetId * REQUEST_CODE_MULTIPLIER + destination.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun allWidgetIds(): IntArray =
        AppWidgetManager.getInstance(context).getAppWidgetIds(
            android.content.ComponentName(context, ForgeFlowWidgetProvider::class.java),
        )

    private fun LocalDate.toWidgetDate(
        today: LocalDate,
        timeMinutes: Int,
    ): String {
        val dayLabel = when (this) {
            today -> context.getString(R.string.widget_today)
            today.plusDays(1) -> context.getString(R.string.widget_tomorrow)
            else -> format(WIDGET_DATE_FORMATTER).replaceFirstChar(Char::uppercase)
        }
        val hour = timeMinutes.coerceIn(0, MINUTES_PER_DAY - 1) / 60
        val minute = timeMinutes.coerceIn(0, MINUTES_PER_DAY - 1) % 60
        return context.getString(
            R.string.widget_scheduled_time,
            dayLabel,
            hour,
            minute,
        )
    }

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

    private fun Long?.toWidgetDuration(): String {
        val minutes = this ?: 0
        return if (minutes < 60) {
            context.getString(R.string.widget_duration_minutes, minutes)
        } else {
            context.getString(
                R.string.widget_duration_hours,
                minutes / 60,
                minutes % 60,
            )
        }
    }

    private fun Double?.toWidgetVolume(): String = NumberFormat.getNumberInstance(
        Locale.forLanguageTag("pt-BR"),
    ).apply { maximumFractionDigits = 1 }.format(this ?: 0.0)

    private companion object {
        const val WIDGET_PROGRESS_MAX = 100
        const val REQUEST_CODE_MULTIPLIER = 10
        const val MINUTES_PER_DAY = 24 * 60
        val WIDGET_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(
            "EEE, dd MMM",
            Locale.forLanguageTag("pt-BR"),
        )
    }
}

private data class ForgeFlowWidgetState(
    val weeklyWorkoutCount: Int,
    val weeklyGoal: Int,
    val currentStreak: Int,
    val nextWorkoutLabel: String?,
    val activeWorkoutName: String?,
    val activeCompletedSets: Int,
    val activeTotalSets: Int,
    val activeExerciseCount: Int,
    val activeStartedAtEpochMillis: Long?,
    val latestWorkoutName: String?,
    val latestWorkoutDurationMinutes: Long?,
    val latestWorkoutVolume: Double?,
    val weightUnitSymbol: String,
    val accentColor: AccentColor,
)

private val com.forgeflow.core.model.WeightUnit.gramsPerDisplayUnit: Double
    get() = when (this) {
        com.forgeflow.core.model.WeightUnit.KILOGRAM -> 1_000.0
        com.forgeflow.core.model.WeightUnit.POUND -> 453.59237
    }

private val com.forgeflow.core.model.WeightUnit.symbol: String
    get() = when (this) {
        com.forgeflow.core.model.WeightUnit.KILOGRAM -> "kg"
        com.forgeflow.core.model.WeightUnit.POUND -> "lb"
    }
