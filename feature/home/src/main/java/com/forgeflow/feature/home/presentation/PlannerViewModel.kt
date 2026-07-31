package com.forgeflow.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.settings.SettingsRepository
import com.forgeflow.core.data.workout.WorkoutRepository
import com.forgeflow.core.model.TrainingDay
import com.forgeflow.core.model.UserSettings
import com.forgeflow.core.model.WorkoutDetails
import com.forgeflow.core.model.calculateWorkoutStreakStats
import com.forgeflow.core.model.nextScheduledWorkoutDate
import com.forgeflow.core.model.toTrainingDay
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PlannerViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    workoutRepository: WorkoutRepository,
) : ViewModel() {
    private val displayedMonth = MutableStateFlow(YearMonth.now())
    private val selectedDate = MutableStateFlow(LocalDate.now())

    val uiState = combine(
        workoutRepository.observeHistory(),
        settingsRepository.observeSettings(),
        displayedMonth,
        selectedDate,
    ) { historyResult, settings, month, selected ->
        val history = (historyResult as? DataResult.Success)?.value.orEmpty()
        createUiState(
            history = history,
            settings = settings,
            month = month,
            selected = selected,
            hasError = historyResult is DataResult.Failure,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlannerUiState(),
    )

    fun onAction(action: PlannerAction) {
        when (action) {
            PlannerAction.PreviousMonth -> moveMonthBy(-1)
            PlannerAction.NextMonth -> moveMonthBy(1)
            is PlannerAction.SelectDate -> {
                val date = LocalDate.ofEpochDay(action.epochDay)
                selectedDate.value = date
                displayedMonth.value = YearMonth.from(date)
            }
            is PlannerAction.WeeklyGoalChanged -> viewModelScope.launch {
                settingsRepository.setWeeklyWorkoutGoal(action.goal)
            }
            is PlannerAction.TrainingDayToggled -> {
                val updatedDays = uiState.value.trainingDays.toMutableSet().apply {
                    if (!add(action.day)) remove(action.day)
                }
                viewModelScope.launch {
                    settingsRepository.setTrainingDays(updatedDays)
                }
            }
            is PlannerAction.PreferredTimeChanged -> viewModelScope.launch {
                settingsRepository.setPreferredWorkoutTime(action.minutesFromMidnight)
            }
        }
    }

    private fun moveMonthBy(months: Long) {
        val targetMonth = displayedMonth.value.plusMonths(months)
        displayedMonth.value = targetMonth
        selectedDate.value = targetMonth.atDay(
            selectedDate.value.dayOfMonth.coerceAtMost(targetMonth.lengthOfMonth()),
        )
    }

    private fun createUiState(
        history: List<WorkoutDetails>,
        settings: UserSettings,
        month: YearMonth,
        selected: LocalDate,
        hasError: Boolean,
    ): PlannerUiState {
        val today = LocalDate.now()
        val historyByDate = history.groupBy { workout ->
            workout.session.startedAt.atZone(ZoneId.systemDefault()).toLocalDate()
        }
        val workoutDates = historyByDate.keys
        val stats = calculateWorkoutStreakStats(workoutDates, today)
        val nextScheduled = nextScheduledWorkoutDate(
            today = today,
            trainingDays = settings.trainingDays,
            completedDates = workoutDates,
        )
        return PlannerUiState(
            isLoading = false,
            hasError = hasError,
            monthLabel = month.format(MONTH_FORMATTER).replaceFirstChar(Char::uppercase),
            firstDayOffset = month.atDay(1).dayOfWeek.value - 1,
            calendarDays = (1..month.lengthOfMonth()).map { day ->
                val date = month.atDay(day)
                PlannerCalendarDayUiModel(
                    epochDay = date.toEpochDay(),
                    dayOfMonth = day,
                    isToday = date == today,
                    isSelected = date == selected,
                    isScheduled = date.dayOfWeek.toTrainingDay() in settings.trainingDays,
                    completedWorkoutCount = historyByDate[date].orEmpty().size,
                )
            },
            selectedDateLabel = selected.format(SELECTED_DATE_FORMATTER)
                .replaceFirstChar(Char::uppercase),
            selectedDayWorkouts = historyByDate[selected]
                .orEmpty()
                .sortedBy { it.session.startedAt }
                .map { workout ->
                    PlannerWorkoutUiModel(
                        name = workout.session.name,
                        time = workout.session.startedAt
                            .atZone(ZoneId.systemDefault())
                            .format(TIME_FORMATTER),
                        completedSets = workout.completedSetCount,
                    )
                },
            isSelectedDayScheduled =
                selected.dayOfWeek.toTrainingDay() in settings.trainingDays,
            weeklyWorkoutGoal = settings.weeklyWorkoutGoal,
            currentWeekWorkouts = stats.currentWeekCount,
            currentStreak = stats.current,
            bestStreak = stats.best,
            trainingDays = settings.trainingDays,
            preferredWorkoutTimeMinutes = settings.preferredWorkoutTimeMinutes,
            nextScheduledWorkout = nextScheduled?.let { date ->
                "${date.format(NEXT_DATE_FORMATTER).replaceFirstChar(Char::uppercase)} • " +
                    settings.preferredWorkoutTimeMinutes.asTimeLabel()
            },
        )
    }

    private fun Int.asTimeLabel(): String = "%02d:%02d".format(this / 60, this % 60)

    private companion object {
        val MONTH_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(
            "MMMM 'de' yyyy",
            Locale.forLanguageTag("pt-BR"),
        )
        val SELECTED_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(
            "EEEE, dd 'de' MMMM",
            Locale.forLanguageTag("pt-BR"),
        )
        val NEXT_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(
            "EEE, dd MMM",
            Locale.forLanguageTag("pt-BR"),
        )
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
