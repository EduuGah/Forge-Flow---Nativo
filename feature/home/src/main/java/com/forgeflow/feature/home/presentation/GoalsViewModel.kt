package com.forgeflow.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.exercise.ExerciseRepository
import com.forgeflow.core.data.goals.GoalRepository
import com.forgeflow.core.data.settings.SettingsRepository
import com.forgeflow.core.data.workout.WorkoutRepository
import com.forgeflow.core.model.Exercise
import com.forgeflow.core.model.ExerciseId
import com.forgeflow.core.model.GoalCadence
import com.forgeflow.core.model.PerformanceGoal
import com.forgeflow.core.model.PerformanceGoalType
import com.forgeflow.core.model.UserSettings
import com.forgeflow.core.model.Weight
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.core.model.WorkoutDetails
import com.forgeflow.core.model.gramsIn
import com.forgeflow.core.model.searchTerms
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    workoutRepository: WorkoutRepository,
    exerciseRepository: ExerciseRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {
    private val localState = MutableStateFlow(GoalsLocalState())
    private val goals = goalRepository.observeGoals().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )
    private val settings = settingsRepository.observeSettings().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        UserSettings(),
    )
    private val exercises = exerciseRepository.observeExercises().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        DataResult.Success(emptyList()),
    )

    val uiState = combine(
        goals,
        workoutRepository.observeHistory(),
        exercises,
        settings,
        localState,
    ) { savedGoals, historyResult, exercisesResult, currentSettings, local ->
        createUiState(savedGoals, historyResult, exercisesResult, currentSettings, local)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GoalsUiState(),
    )

    fun onAction(action: GoalsAction) {
        when (action) {
            is GoalsAction.FilterChanged -> updateLocal { copy(filter = action.filter) }
            is GoalsAction.OpenEditor -> openEditor(action.goalId)
            GoalsAction.CloseEditor -> updateLocal { copy(editor = null) }
            is GoalsAction.TypeChanged -> updateEditor {
                copy(
                    type = action.type,
                    cadence = if (action.type == PerformanceGoalType.EXERCISE_WEIGHT) {
                        GoalCadence.ONCE
                    } else {
                        cadence.takeUnless { it == GoalCadence.ONCE } ?: GoalCadence.WEEKLY
                    },
                    validationFailed = false,
                )
            }
            is GoalsAction.CadenceChanged -> updateEditor {
                copy(cadence = action.cadence, validationFailed = false)
            }
            is GoalsAction.TitleChanged -> updateEditor { copy(title = action.value) }
            is GoalsAction.TargetChanged -> updateEditor {
                copy(target = action.value.numericInput(), validationFailed = false)
            }
            is GoalsAction.MinimumRepetitionsChanged -> updateEditor {
                copy(
                    minimumRepetitions = action.value.filter(Char::isDigit).take(3),
                    validationFailed = false,
                )
            }
            is GoalsAction.ExerciseSelected -> selectExercise(action.exerciseId)
            is GoalsAction.DeadlineEnabledChanged -> updateEditor {
                copy(hasDeadline = action.enabled, validationFailed = false)
            }
            is GoalsAction.DeadlineChanged -> updateEditor {
                copy(deadlineEpochDay = action.epochDay, validationFailed = false)
            }
            GoalsAction.Save -> saveGoal()
            is GoalsAction.Delete -> deleteGoal(action.goalId)
            GoalsAction.DismissWriteError -> updateLocal { copy(writeFailed = false) }
        }
    }

    private fun openEditor(goalId: String?) {
        val saved = goals.value.firstOrNull { it.id == goalId }
        val today = LocalDate.now()
        val editor = if (saved == null) {
            GoalEditorUiState(deadlineEpochDay = today.plusMonths(1).toEpochDay())
        } else {
            GoalEditorUiState(
                id = saved.id,
                type = saved.type,
                cadence = saved.cadence,
                title = saved.title,
                target = saved.targetValue.asEditorValue(saved.type, settings.value.weightUnit),
                exerciseId = saved.exerciseId?.value,
                exerciseName = saved.exerciseNameSnapshot,
                minimumRepetitions = saved.minimumRepetitions.toString(),
                hasDeadline = saved.deadlineEpochDay != null,
                deadlineEpochDay = saved.deadlineEpochDay ?: today.plusMonths(1).toEpochDay(),
            )
        }
        updateLocal { copy(editor = editor, writeFailed = false) }
    }

    private fun selectExercise(exerciseId: String) {
        val exercise = (exercises.value as? DataResult.Success)
            ?.value
            ?.firstOrNull { it.id.value == exerciseId }
            ?: return
        updateEditor {
            copy(
                exerciseId = exercise.id.value,
                exerciseName = exercise.name,
                validationFailed = false,
            )
        }
    }

    private fun saveGoal() {
        val editor = localState.value.editor ?: return
        val target = editor.target.replace(',', '.').toDoubleOrNull()
        val repetitions = editor.minimumRepetitions.toIntOrNull()
        val deadline = editor.deadlineEpochDay.takeIf { editor.hasDeadline }
        val today = LocalDate.now().toEpochDay()
        if (
            target == null || target <= 0.0 ||
            (editor.type == PerformanceGoalType.EXERCISE_WEIGHT && editor.exerciseId == null) ||
            repetitions == null || repetitions <= 0 ||
            (deadline != null && deadline < today)
        ) {
            updateEditor { copy(validationFailed = true) }
            return
        }
        val oldGoal = goals.value.firstOrNull { it.id == editor.id }
        val targetValue = when (editor.type) {
            PerformanceGoalType.EXERCISE_WEIGHT,
            PerformanceGoalType.TOTAL_VOLUME,
            -> Weight.from(target, settings.value.weightUnit).grams
            PerformanceGoalType.WORKOUT_COUNT,
            PerformanceGoalType.TRAINING_DURATION,
            -> target.toLong()
        }
        if (targetValue <= 0L) {
            updateEditor { copy(validationFailed = true) }
            return
        }
        val cadence = if (editor.type == PerformanceGoalType.EXERCISE_WEIGHT) {
            GoalCadence.ONCE
        } else {
            editor.cadence
        }
        val title = editor.title.trim().ifBlank {
            defaultTitle(editor, target, settings.value.weightUnit)
        }
        val goal = PerformanceGoal(
            id = oldGoal?.id ?: UUID.randomUUID().toString(),
            type = editor.type,
            cadence = cadence,
            title = title,
            targetValue = targetValue,
            exerciseId = editor.exerciseId?.let(::ExerciseId),
            exerciseNameSnapshot = editor.exerciseName,
            minimumRepetitions = repetitions,
            deadlineEpochDay = deadline,
            createdAt = oldGoal?.createdAt ?: Instant.now(),
        )
        updateEditor { copy(isSaving = true) }
        viewModelScope.launch {
            when (goalRepository.saveGoal(goal)) {
                is DataResult.Success -> updateLocal { copy(editor = null, writeFailed = false) }
                is DataResult.Failure -> updateLocal {
                    copy(editor = editor.copy(isSaving = false), writeFailed = true)
                }
            }
        }
    }

    private fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            when (goalRepository.deleteGoal(goalId)) {
                is DataResult.Success -> updateLocal { copy(writeFailed = false) }
                is DataResult.Failure -> updateLocal { copy(writeFailed = true) }
            }
        }
    }

    private fun createUiState(
        goals: List<PerformanceGoal>,
        historyResult: DataResult<List<WorkoutDetails>>,
        exercisesResult: DataResult<List<Exercise>>,
        settings: UserSettings,
        local: GoalsLocalState,
    ): GoalsUiState {
        val history = (historyResult as? DataResult.Success)?.value.orEmpty()
        val mapped = goals.map { goal -> goal.toUiModel(history, settings.weightUnit) }
        val visible = mapped.filter { goal ->
            when (local.filter) {
                GoalsFilter.ACTIVE -> !goal.isCompleted
                GoalsFilter.ALL -> true
                GoalsFilter.COMPLETED -> goal.isCompleted
            }
        }.sortedWith(
            compareByDescending<GoalUiModel> { it.isOverdue }
                .thenBy { it.isCompleted }
                .thenByDescending { it.progress },
        )
        return GoalsUiState(
            isLoading = false,
            hasHistoryError = historyResult is DataResult.Failure,
            filter = local.filter,
            goals = visible,
            activeCount = mapped.count { !it.isCompleted },
            completedCount = mapped.count(GoalUiModel::isCompleted),
            overdueCount = mapped.count(GoalUiModel::isOverdue),
            editor = local.editor,
            exercises = (exercisesResult as? DataResult.Success)
                ?.value
                .orEmpty()
                .sortedBy(Exercise::name)
                .map(Exercise::toGoalExerciseUiModel),
            weightUnit = settings.weightUnit,
            writeFailed = local.writeFailed,
        )
    }

    private fun PerformanceGoal.toUiModel(
        history: List<WorkoutDetails>,
        weightUnit: WeightUnit,
    ): GoalUiModel {
        val progress = calculateGoalProgress(this, history)
        return GoalUiModel(
            id = id,
            title = title,
            type = type,
            cadence = cadence,
            progress = progress.fraction,
            progressLabel = progressLabel(progress.currentValue, targetValue, type, weightUnit),
            cadenceLabel = cadence.label(),
            deadlineLabel = deadlineEpochDay?.let {
                LocalDate.ofEpochDay(it).format(DEADLINE_FORMATTER)
            },
            exerciseMediaUri = exerciseId?.let { id ->
                (exercises.value as? DataResult.Success)
                    ?.value
                    ?.firstOrNull { it.id == id }
                    ?.media
                    ?.let { it.thumbnailUri ?: it.uri }
            },
            isCompleted = progress.isCompleted,
            isOverdue = progress.isOverdue,
        )
    }

    private fun updateLocal(block: GoalsLocalState.() -> GoalsLocalState) {
        localState.value = localState.value.block()
    }

    private fun updateEditor(block: GoalEditorUiState.() -> GoalEditorUiState) {
        updateLocal { copy(editor = editor?.block()) }
    }
}

private data class GoalsLocalState(
    val filter: GoalsFilter = GoalsFilter.ACTIVE,
    val editor: GoalEditorUiState? = null,
    val writeFailed: Boolean = false,
)

private fun Exercise.toGoalExerciseUiModel() = GoalExerciseUiModel(
    id = id.value,
    name = name,
    searchTerms = searchTerms(),
    muscleGroup = primaryMuscleGroup,
    equipment = equipment,
    mediaUri = media?.let { it.thumbnailUri ?: it.uri },
)

private fun Long.asEditorValue(type: PerformanceGoalType, unit: WeightUnit): String = when (type) {
    PerformanceGoalType.EXERCISE_WEIGHT,
    PerformanceGoalType.TOTAL_VOLUME,
    -> gramsIn(unit).cleanNumber()
    PerformanceGoalType.WORKOUT_COUNT,
    PerformanceGoalType.TRAINING_DURATION,
    -> toString()
}

private fun defaultTitle(
    editor: GoalEditorUiState,
    target: Double,
    unit: WeightUnit,
): String = when (editor.type) {
    PerformanceGoalType.EXERCISE_WEIGHT ->
        "${target.cleanNumber()} ${unit.shortLabel()} no ${editor.exerciseName.orEmpty()}"
    PerformanceGoalType.WORKOUT_COUNT -> "${target.toLong()} treinos"
    PerformanceGoalType.TOTAL_VOLUME ->
        "${target.cleanNumber()} ${unit.shortLabel()} de volume"
    PerformanceGoalType.TRAINING_DURATION -> "${target.toLong()} min em treino"
}

private fun progressLabel(
    current: Long,
    target: Long,
    type: PerformanceGoalType,
    unit: WeightUnit,
): String = when (type) {
    PerformanceGoalType.EXERCISE_WEIGHT,
    PerformanceGoalType.TOTAL_VOLUME,
    -> "${current.gramsIn(unit).cleanNumber()} / ${target.gramsIn(unit).cleanNumber()} ${unit.shortLabel()}"
    PerformanceGoalType.WORKOUT_COUNT -> "$current / $target treinos"
    PerformanceGoalType.TRAINING_DURATION -> "$current / $target min"
}

private fun GoalCadence.label(): String = when (this) {
    GoalCadence.ONCE -> "Meta única"
    GoalCadence.DAILY -> "Diária"
    GoalCadence.WEEKLY -> "Semanal"
    GoalCadence.MONTHLY -> "Mensal"
}

private fun WeightUnit.shortLabel(): String = when (this) {
    WeightUnit.KILOGRAM -> "kg"
    WeightUnit.POUND -> "lb"
}

private fun Double.cleanNumber(): String = if (this % 1.0 == 0.0) {
    NumberFormat.getIntegerInstance(Locale.forLanguageTag("pt-BR")).format(toLong())
} else {
    NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR")).apply {
        maximumFractionDigits = 2
    }.format(this)
}

private fun String.numericInput(): String {
    val normalized = replace('.', ',').filter { it.isDigit() || it == ',' }
    val separator = normalized.indexOf(',')
    return if (separator < 0) normalized else {
        normalized.take(separator + 1) + normalized.drop(separator + 1).replace(",", "")
    }
}

private val DEADLINE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(
    "dd MMM yyyy",
    Locale.forLanguageTag("pt-BR"),
)
