package com.forgeflow.feature.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.settings.SettingsRepository
import com.forgeflow.core.data.workout.WorkoutRepository
import com.forgeflow.core.model.PersonalRecordType
import com.forgeflow.core.model.UserSettings
import com.forgeflow.core.model.WorkoutDetails
import com.forgeflow.core.model.WorkoutSessionId
import com.forgeflow.core.model.WorkoutSet
import com.forgeflow.core.model.gramsIn
import com.forgeflow.core.model.personalRecordsAgainst
import com.forgeflow.core.platform.health.HealthConnectManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: WorkoutRepository,
    settingsRepository: SettingsRepository,
    private val healthConnectManager: HealthConnectManager,
) : ViewModel() {
    private val filters = MutableStateFlow(HistoryFilters())
    private val pendingDeleteId = MutableStateFlow<String?>(null)
    private val isDeleting = MutableStateFlow(false)

    val uiState = combine(
        repository.observeHistory(),
        settingsRepository.observeSettings(),
        filters,
        combine(pendingDeleteId, isDeleting) { pendingId, deleting ->
            pendingId to deleting
        },
    ) { result, settings, currentFilters, (pendingId, deleting) ->
        when (result) {
            is DataResult.Failure -> HistoryUiState(
                isLoading = false,
                searchQuery = currentFilters.searchQuery,
                dateFilter = currentFilters.dateFilter,
                onlyWithLocation = currentFilters.onlyWithLocation,
                error = true,
            )
            is DataResult.Success -> result.value.toUiState(
                settings = settings,
                filters = currentFilters,
                pendingDeleteId = pendingId,
                deleting = deleting,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(),
    )

    fun onAction(action: HistoryAction) {
        when (action) {
            is HistoryAction.SearchChanged -> filters.update {
                it.copy(searchQuery = action.query)
            }
            is HistoryAction.DateFilterChanged -> filters.update {
                it.copy(dateFilter = action.filter)
            }
            is HistoryAction.OnlyWithLocationChanged -> filters.update {
                it.copy(onlyWithLocation = action.enabled)
            }
            HistoryAction.ClearFilters -> filters.value = HistoryFilters()
            is HistoryAction.DeleteRequested -> pendingDeleteId.value = action.workoutId
            HistoryAction.DeleteDismissed -> pendingDeleteId.value = null
            HistoryAction.DeleteConfirmed -> deletePendingWorkout()
        }
    }

    private fun deletePendingWorkout() {
        val workoutId = pendingDeleteId.value ?: return
        viewModelScope.launch {
            isDeleting.value = true
            if (
                repository.deleteCompletedWorkout(WorkoutSessionId(workoutId))
                    is DataResult.Success
            ) {
                healthConnectManager.deleteWorkout(workoutId)
                pendingDeleteId.value = null
            }
            isDeleting.value = false
        }
    }

    private fun List<WorkoutDetails>.toUiState(
        settings: UserSettings,
        filters: HistoryFilters,
        pendingDeleteId: String?,
        deleting: Boolean,
    ): HistoryUiState {
        val personalRecords = calculatePersonalRecords()
        val allItems = map { workout -> workout.toUiModel(settings, personalRecords) }
        val cutoff = filters.dateFilter.cutoff()
        val normalizedQuery = filters.searchQuery.trim().lowercase()
        val items = allItems.filter { item ->
            val matchesDate = cutoff == null ||
                item.startedAtEpochMillis >= cutoff.toEpochMilli()
            val matchesLocation = !filters.onlyWithLocation || item.hasLocation
            val matchesQuery = normalizedQuery.isBlank() ||
                item.name.lowercase().contains(normalizedQuery) ||
                item.searchableDate.lowercase().contains(normalizedQuery) ||
                item.locationLabel.orEmpty().lowercase().contains(normalizedQuery) ||
                item.exercises.any { it.name.lowercase().contains(normalizedQuery) }
            matchesDate && matchesLocation && matchesQuery
        }
        return HistoryUiState(
            isLoading = false,
            workouts = items,
            totalSets = items.sumOf(HistoryWorkoutUiModel::completedSets),
            totalVolume = items.sumOf(HistoryWorkoutUiModel::volume),
            totalDurationMinutes = items.sumOf(HistoryWorkoutUiModel::durationMinutes),
            weightUnit = settings.weightUnit,
            searchQuery = filters.searchQuery,
            dateFilter = filters.dateFilter,
            onlyWithLocation = filters.onlyWithLocation,
            mapPoints = items.mapNotNull { item ->
                val latitude = item.latitude ?: return@mapNotNull null
                val longitude = item.longitude ?: return@mapNotNull null
                HistoryMapPointUiModel(
                    latitude = latitude,
                    longitude = longitude,
                    label = item.locationLabel ?: item.name,
                )
            },
            pendingDeleteWorkout = allItems.firstOrNull { it.id == pendingDeleteId },
            isDeleting = deleting,
        )
    }

    private fun List<WorkoutDetails>.calculatePersonalRecords():
        Map<String, Set<PersonalRecordType>> {
        val previousByExercise = mutableMapOf<String, MutableList<WorkoutSet>>()
        val recordsBySet = mutableMapOf<String, Set<PersonalRecordType>>()
        asReversed().forEach { workout ->
            workout.exercises.forEach { exercise ->
                val exerciseKey = exercise.sessionExercise.exerciseId?.value
                    ?: exercise.sessionExercise.exerciseNameSnapshot
                val previous = previousByExercise.getOrPut(exerciseKey) { mutableListOf() }
                exercise.sets
                    .filter(WorkoutSet::isCompleted)
                    .sortedBy(WorkoutSet::position)
                    .forEach { set ->
                        val records = set.personalRecordsAgainst(previous)
                        if (records.isNotEmpty()) {
                            recordsBySet[set.id.value] = records
                        }
                        previous += set
                    }
            }
        }
        return recordsBySet
    }

    private fun WorkoutDetails.toUiModel(
        settings: UserSettings,
        personalRecords: Map<String, Set<PersonalRecordType>>,
    ): HistoryWorkoutUiModel {
        val date = session.startedAt.atZone(ZoneId.systemDefault())
        return HistoryWorkoutUiModel(
            id = session.id.value,
            name = session.name,
            day = DAY_FORMATTER.format(date),
            month = MONTH_FORMATTER.format(date).uppercase(),
            time = TIME_FORMATTER.format(date),
            monthGroup = MONTH_GROUP_FORMATTER.format(date),
            startedAtEpochMillis = session.startedAt.toEpochMilli(),
            durationMinutes = duration().toMinutes(),
            volume = totalVolumeGrams.gramsIn(settings.weightUnit),
            exerciseCount = exercises.size,
            completedSets = exercises.sumOf { exercise ->
                exercise.sets.count {
                    it.isCompleted && it.repetitions.count > 0
                }
            },
            hasLocation = session.location != null,
            locationLabel = session.location?.label,
            latitude = session.location?.latitude,
            longitude = session.location?.longitude,
            searchableDate = SEARCH_DATE_FORMATTER.format(date),
            exercises = exercises.map { exercise ->
                val completed = exercise.sets.filter {
                    it.isCompleted && it.repetitions.count > 0
                }
                val best = completed.maxByOrNull { it.weight.grams }
                HistoryExerciseUiModel(
                    exerciseId = exercise.sessionExercise.exerciseId?.value,
                    name = exercise.sessionExercise.exerciseNameSnapshot,
                    mediaUri = exercise.sessionExercise.mediaUriSnapshot
                        ?: exercise.exercise?.media?.uri,
                    mediaType = exercise.sessionExercise.mediaTypeSnapshot
                        ?: exercise.exercise?.media?.type,
                    mediaThumbnailUri = exercise.sessionExercise.mediaThumbnailUriSnapshot
                        ?: exercise.exercise?.media?.thumbnailUri,
                    completedSets = completed.size,
                    totalVolume = completed.sumOf {
                        it.weight.grams * it.repetitions.count
                    }.gramsIn(settings.weightUnit),
                    bestWeight = best?.weight?.valueIn(settings.weightUnit),
                    bestRepetitions = best?.repetitions?.count,
                    personalRecordCount = completed.sumOf {
                        personalRecords[it.id.value].orEmpty().size
                    },
                    sets = completed.mapIndexed { index, set ->
                        HistorySetUiModel(
                            id = set.id.value,
                            number = index + 1,
                            type = set.setType,
                            weight = set.weight.valueIn(settings.weightUnit),
                            repetitions = set.repetitions.count,
                            personalRecordTypes = personalRecords[set.id.value].orEmpty(),
                        )
                    },
                )
            },
        )
    }

    private fun WorkoutDetails.duration(): Duration =
        session.finishedAt?.let { Duration.between(session.startedAt, it) }
            ?: Duration.ZERO

    private fun HistoryDateFilter.cutoff(): Instant? = when (this) {
        HistoryDateFilter.ALL -> null
        HistoryDateFilter.LAST_7_DAYS -> Instant.now().minus(7, ChronoUnit.DAYS)
        HistoryDateFilter.LAST_30_DAYS -> Instant.now().minus(30, ChronoUnit.DAYS)
        HistoryDateFilter.LAST_90_DAYS -> Instant.now().minus(90, ChronoUnit.DAYS)
    }

    private data class HistoryFilters(
        val searchQuery: String = "",
        val dateFilter: HistoryDateFilter = HistoryDateFilter.ALL,
        val onlyWithLocation: Boolean = false,
    )

    private companion object {
        val DAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd")
        val MONTH_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(
            "MMM",
            Locale.forLanguageTag("pt-BR"),
        )
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val MONTH_GROUP_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(
            "MMMM 'de' yyyy",
            Locale.forLanguageTag("pt-BR"),
        )
        val SEARCH_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(
            "dd/MM/yyyy",
            Locale.forLanguageTag("pt-BR"),
        )
    }
}
