package com.forgeflow.feature.nutrition.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.nutrition.NutritionRepository
import com.forgeflow.core.model.NutritionGoals
import com.forgeflow.core.model.NutritionJournal
import com.forgeflow.core.model.NutritionMeal
import com.forgeflow.core.model.NutritionMealType
import com.forgeflow.core.model.HydrationEntry
import com.forgeflow.core.model.NutritionReminderSettings
import com.forgeflow.core.platform.notification.WellnessReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val repository: NutritionRepository,
    private val wellnessReminderScheduler: WellnessReminderScheduler,
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val operation = MutableStateFlow(NutritionOperationState())

    val uiState = combine(
        repository.observeJournal(),
        selectedDate,
        operation,
    ) { journal, date, currentOperation ->
        journal.asUiState(date, currentOperation)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NutritionUiState(),
    )

    fun onAction(action: NutritionAction) {
        when (action) {
            NutritionAction.PreviousDay -> selectedDate.update { it.minusDays(1) }
            NutritionAction.NextDay -> selectedDate.update { date ->
                if (date.isBefore(LocalDate.now())) date.plusDays(1) else date
            }
            NutritionAction.Today -> selectedDate.value = LocalDate.now()
            is NutritionAction.DateSelected -> {
                val requestedDate = LocalDate.ofEpochDay(action.epochDay)
                if (!requestedDate.isAfter(LocalDate.now())) {
                    selectedDate.value = requestedDate
                }
            }
            NutritionAction.OpenMealEditor -> operation.update {
                it.copy(mealEditor = NutritionMealEditorUiState(), writeFailed = false)
            }
            NutritionAction.CloseMealEditor -> operation.update { it.copy(mealEditor = null) }
            is NutritionAction.MealNameChanged -> updateMealEditor {
                copy(name = action.value.take(MAX_NAME_LENGTH))
            }
            is NutritionAction.MealTypeChanged -> updateMealEditor { copy(type = action.value) }
            is NutritionAction.MealCaloriesChanged -> updateMealEditor {
                copy(calories = action.value.numericInput(MAX_CALORIES_LENGTH))
            }
            is NutritionAction.MealProteinChanged -> updateMealEditor {
                copy(proteinGrams = action.value.decimalInput())
            }
            is NutritionAction.MealCarbohydrateChanged -> updateMealEditor {
                copy(carbohydrateGrams = action.value.decimalInput())
            }
            is NutritionAction.MealFatChanged -> updateMealEditor {
                copy(fatGrams = action.value.decimalInput())
            }
            is NutritionAction.MealNotesChanged -> updateMealEditor {
                copy(notes = action.value.take(MAX_NOTES_LENGTH))
            }
            is NutritionAction.MealPhotoSelected -> updateMealEditor {
                copy(sourcePhotoUri = action.sourceUri)
            }
            NutritionAction.CalculateCaloriesFromMacros -> calculateCalories()
            NutritionAction.SaveMeal -> saveMeal()
            is NutritionAction.DeleteMeal -> deleteMeal(action.mealId)
            NutritionAction.OpenGoalsEditor -> openGoalsEditor()
            NutritionAction.CloseGoalsEditor -> operation.update { it.copy(goalsEditor = null) }
            is NutritionAction.GoalCaloriesChanged -> updateGoalsEditor {
                copy(calories = action.value.numericInput(MAX_CALORIES_LENGTH))
            }
            is NutritionAction.GoalProteinChanged -> updateGoalsEditor {
                copy(proteinGrams = action.value.decimalInput())
            }
            is NutritionAction.GoalCarbohydrateChanged -> updateGoalsEditor {
                copy(carbohydrateGrams = action.value.decimalInput())
            }
            is NutritionAction.GoalFatChanged -> updateGoalsEditor {
                copy(fatGrams = action.value.decimalInput())
            }
            is NutritionAction.GoalWaterChanged -> updateGoalsEditor {
                copy(waterMilliliters = action.value.numericInput(MAX_WATER_LENGTH))
            }
            NutritionAction.SaveGoals -> saveGoals()
            is NutritionAction.AddWater -> addWater(action.milliliters)
            NutritionAction.RemoveLastWater -> removeLastWater()
            is NutritionAction.WellnessRemindersChanged -> {
                saveReminderSettings(enabled = action.enabled)
            }
            is NutritionAction.WellnessReminderIntervalChanged -> {
                saveReminderSettings(intervalHours = action.hours)
            }
            NutritionAction.PhotoCaptureFailed -> operation.update {
                it.copy(writeFailed = true)
            }
            NutritionAction.DismissError -> operation.update { it.copy(writeFailed = false) }
        }
    }

    private fun updateMealEditor(
        transform: NutritionMealEditorUiState.() -> NutritionMealEditorUiState,
    ) {
        operation.update { current ->
            current.copy(mealEditor = current.mealEditor?.transform())
        }
    }

    private fun updateGoalsEditor(
        transform: NutritionGoalsEditorUiState.() -> NutritionGoalsEditorUiState,
    ) {
        operation.update { current ->
            current.copy(goalsEditor = current.goalsEditor?.transform())
        }
    }

    private fun calculateCalories() {
        updateMealEditor {
            val protein = proteinGrams.decimalValue()
            val carbohydrate = carbohydrateGrams.decimalValue()
            val fat = fatGrams.decimalValue()
            copy(
                calories = caloriesFromMacros(
                    proteinGrams = protein,
                    carbohydrateGrams = carbohydrate,
                    fatGrams = fat,
                ).toString(),
            )
        }
    }

    private fun saveMeal() {
        val editor = operation.value.mealEditor ?: return
        if (editor.isSaving || editor.name.isBlank()) return
        viewModelScope.launch {
            updateMealEditor { copy(isSaving = true) }
            val eatenAt = selectedDate.value
                .atTime(LocalTime.now())
                .atZone(ZoneId.systemDefault())
                .toInstant()
            val result = repository.saveMeal(
                meal = NutritionMeal(
                    id = UUID.randomUUID().toString(),
                    name = editor.name.trim(),
                    type = editor.type,
                    calories = editor.calories.toIntOrNull()?.coerceAtLeast(0) ?: 0,
                    proteinGrams = editor.proteinGrams.decimalValue(),
                    carbohydrateGrams = editor.carbohydrateGrams.decimalValue(),
                    fatGrams = editor.fatGrams.decimalValue(),
                    notes = editor.notes.trim(),
                    photoPath = null,
                    eatenAt = eatenAt,
                ),
                sourcePhotoUri = editor.sourcePhotoUri,
            )
            operation.update {
                it.copy(
                    mealEditor = if (result is DataResult.Success) null else editor.copy(
                        isSaving = false,
                    ),
                    writeFailed = result is DataResult.Failure,
                )
            }
        }
    }

    private fun deleteMeal(mealId: String) {
        viewModelScope.launch {
            val result = repository.deleteMeal(mealId)
            if (result is DataResult.Failure) {
                operation.update { it.copy(writeFailed = true) }
            }
        }
    }

    private fun openGoalsEditor() {
        viewModelScope.launch {
            val goals = repository.observeJournal().first().goals
            operation.update {
                it.copy(
                    goalsEditor = NutritionGoalsEditorUiState(
                        calories = goals.calories.toString(),
                        proteinGrams = goals.proteinGrams.toInputValue(),
                        carbohydrateGrams = goals.carbohydrateGrams.toInputValue(),
                        fatGrams = goals.fatGrams.toInputValue(),
                        waterMilliliters = goals.waterMilliliters.toString(),
                    ),
                    writeFailed = false,
                )
            }
        }
    }

    private fun saveGoals() {
        val editor = operation.value.goalsEditor ?: return
        if (editor.isSaving) return
        viewModelScope.launch {
            updateGoalsEditor { copy(isSaving = true) }
            val result = repository.saveGoals(
                NutritionGoals(
                    calories = editor.calories.toIntOrNull()?.coerceAtLeast(1) ?: 2_200,
                    proteinGrams = editor.proteinGrams.decimalValue().coerceAtLeast(1.0),
                    carbohydrateGrams = editor.carbohydrateGrams.decimalValue().coerceAtLeast(1.0),
                    fatGrams = editor.fatGrams.decimalValue().coerceAtLeast(1.0),
                    waterMilliliters = editor.waterMilliliters.toIntOrNull()
                        ?.coerceIn(500, 10_000)
                        ?: 2_500,
                ),
            )
            operation.update {
                it.copy(
                    goalsEditor = if (result is DataResult.Success) null else editor.copy(
                        isSaving = false,
                    ),
                    writeFailed = result is DataResult.Failure,
                )
            }
        }
    }

    private fun addWater(milliliters: Int) {
        if (milliliters !in 50..2_000 || selectedDate.value.isAfter(LocalDate.now())) return
        viewModelScope.launch {
            val consumedAt = selectedDate.value
                .atTime(LocalTime.now())
                .atZone(ZoneId.systemDefault())
                .toInstant()
            repository.addHydration(
                HydrationEntry(
                    id = UUID.randomUUID().toString(),
                    milliliters = milliliters,
                    consumedAt = consumedAt,
                ),
            )
        }
    }

    private fun removeLastWater() {
        val id = uiState.value.lastHydrationEntryId ?: return
        viewModelScope.launch { repository.removeHydration(id) }
    }

    private fun saveReminderSettings(
        enabled: Boolean = uiState.value.wellnessRemindersEnabled,
        intervalHours: Int = uiState.value.wellnessReminderIntervalHours,
    ) {
        val settings = NutritionReminderSettings(
            enabled = enabled,
            intervalHours = intervalHours.coerceIn(1, 12),
        )
        viewModelScope.launch {
            if (repository.saveReminderSettings(settings) is DataResult.Success) {
                if (settings.enabled) {
                    wellnessReminderScheduler.schedule(settings.intervalHours)
                } else {
                    wellnessReminderScheduler.cancel()
                }
            }
        }
    }

    private fun NutritionJournal.asUiState(
        date: LocalDate,
        currentOperation: NutritionOperationState,
    ): NutritionUiState {
        val zone = ZoneId.systemDefault()
        val dailyMeals = meals.filter { meal ->
            meal.eatenAt.atZone(zone).toLocalDate() == date
        }
        val dailyHydration = hydration.filter { entry ->
            entry.consumedAt.atZone(zone).toLocalDate() == date
        }
        return NutritionUiState(
            isLoading = false,
            dateLabel = DATE_FORMATTER.format(date),
            selectedDateEpochDay = date.toEpochDay(),
            isToday = date == LocalDate.now(),
            calories = dailyMeals.sumOf(NutritionMeal::calories),
            calorieGoal = goals.calories,
            proteinGrams = dailyMeals.sumOf(NutritionMeal::proteinGrams),
            proteinGoalGrams = goals.proteinGrams,
            carbohydrateGrams = dailyMeals.sumOf(NutritionMeal::carbohydrateGrams),
            carbohydrateGoalGrams = goals.carbohydrateGrams,
            fatGrams = dailyMeals.sumOf(NutritionMeal::fatGrams),
            fatGoalGrams = goals.fatGrams,
            waterMilliliters = dailyHydration.sumOf(HydrationEntry::milliliters),
            waterGoalMilliliters = goals.waterMilliliters,
            lastHydrationEntryId = dailyHydration.maxByOrNull(HydrationEntry::consumedAt)?.id,
            wellnessRemindersEnabled = reminders.enabled,
            wellnessReminderIntervalHours = reminders.intervalHours,
            meals = dailyMeals.map { meal ->
                NutritionMealUiModel(
                    id = meal.id,
                    name = meal.name,
                    type = meal.type,
                    typeLabel = meal.type.label,
                    timeLabel = TIME_FORMATTER.format(meal.eatenAt.atZone(zone)),
                    calories = meal.calories,
                    macrosLabel = "P ${meal.proteinGrams.toInputValue()} g • " +
                        "C ${meal.carbohydrateGrams.toInputValue()} g • " +
                        "G ${meal.fatGrams.toInputValue()} g",
                    notes = meal.notes,
                    photoPath = meal.photoPath,
                )
            },
            mealEditor = currentOperation.mealEditor,
            goalsEditor = currentOperation.goalsEditor,
            writeFailed = currentOperation.writeFailed,
        )
    }

    private fun String.numericInput(maxLength: Int): String =
        filter(Char::isDigit).take(maxLength)

    private fun String.decimalInput(): String {
        var separatorFound = false
        return replace(',', '.')
            .filterIndexed { index, character ->
                when {
                    character.isDigit() -> true
                    character == '.' && index > 0 && !separatorFound -> {
                        separatorFound = true
                        true
                    }
                    else -> false
                }
            }
            .take(MAX_MACRO_LENGTH)
    }

    private fun String.decimalValue(): Double = replace(',', '.').toDoubleOrNull() ?: 0.0

    private fun Double.toInputValue(): String =
        if (this % 1.0 == 0.0) toInt().toString() else "%.1f".format(Locale.US, this)

    private val NutritionMealType.label: String
        get() = when (this) {
            NutritionMealType.BREAKFAST -> "Café da manhã"
            NutritionMealType.LUNCH -> "Almoço"
            NutritionMealType.SNACK -> "Lanche"
            NutritionMealType.DINNER -> "Jantar"
            NutritionMealType.PRE_WORKOUT -> "Pré-treino"
            NutritionMealType.POST_WORKOUT -> "Pós-treino"
            NutritionMealType.OTHER -> "Outro"
        }

    private data class NutritionOperationState(
        val mealEditor: NutritionMealEditorUiState? = null,
        val goalsEditor: NutritionGoalsEditorUiState? = null,
        val writeFailed: Boolean = false,
    )

    private companion object {
        const val MAX_NAME_LENGTH = 80
        const val MAX_NOTES_LENGTH = 280
        const val MAX_CALORIES_LENGTH = 5
        const val MAX_MACRO_LENGTH = 6
        const val MAX_WATER_LENGTH = 5
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(
            "EEEE, dd 'de' MMMM",
            Locale.forLanguageTag("pt-BR"),
        )
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
