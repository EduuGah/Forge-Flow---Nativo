package com.forgeflow.feature.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.settings.SettingsRepository
import com.forgeflow.core.data.workout.WorkoutRepository
import com.forgeflow.core.model.UserSettings
import com.forgeflow.core.model.WorkoutDetails
import com.forgeflow.core.model.gramsIn
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

@HiltViewModel
class TrainingMapViewModel @Inject constructor(
    workoutRepository: WorkoutRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {
    private val selectedPeriod = MutableStateFlow(TrainingMapPeriod.ALL)
    private val selectedPlaceId = MutableStateFlow<String?>(null)

    val uiState = combine(
        workoutRepository.observeHistory(),
        settingsRepository.observeSettings(),
        selectedPeriod,
        selectedPlaceId,
    ) { historyResult, settings, period, placeId ->
        when (historyResult) {
            is DataResult.Failure -> TrainingMapUiState(
                isLoading = false,
                hasError = true,
                period = period,
                weightUnit = settings.weightUnit,
            )
            is DataResult.Success -> historyResult.value.toTrainingMapUiState(
                settings = settings,
                period = period,
                requestedPlaceId = placeId,
                now = Instant.now(),
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrainingMapUiState(),
    )

    fun onAction(action: TrainingMapAction) {
        when (action) {
            is TrainingMapAction.PeriodChanged -> {
                selectedPeriod.value = action.period
                selectedPlaceId.value = null
            }
            is TrainingMapAction.PlaceSelected -> selectedPlaceId.value = action.placeId
        }
    }
}

private fun List<WorkoutDetails>.toTrainingMapUiState(
    settings: UserSettings,
    period: TrainingMapPeriod,
    requestedPlaceId: String?,
    now: Instant,
): TrainingMapUiState {
    val cutoff = period.days?.let { now.minus(it, ChronoUnit.DAYS) }
    val locatedWorkouts = filter { workout ->
        workout.session.location != null &&
            (cutoff == null || !workout.session.startedAt.isBefore(cutoff))
    }
    val byId = locatedWorkouts.associateBy { it.session.id.value }
    val clusters = clusterTrainingLocations(
        locatedWorkouts.mapNotNull { workout ->
            val location = workout.session.location ?: return@mapNotNull null
            TrainingLocationSample(
                workoutId = workout.session.id.value,
                label = location.label,
                latitude = location.latitude,
                longitude = location.longitude,
            )
        },
    )
    val places = clusters.map { cluster ->
        val workouts = cluster.samples.mapNotNull { byId[it.workoutId] }
            .sortedByDescending { it.session.startedAt }
        TrainingPlaceUiModel(
            id = cluster.id,
            label = cluster.label,
            latitude = cluster.latitude,
            longitude = cluster.longitude,
            workoutCount = workouts.size,
            totalVolume = workouts.sumOf(WorkoutDetails::totalVolumeGrams)
                .gramsIn(settings.weightUnit),
            totalDurationMinutes = workouts.sumOf(WorkoutDetails::durationMinutes),
            lastVisited = workouts.firstOrNull()?.let { workout ->
                DATE_FORMATTER.format(
                    workout.session.startedAt.atZone(ZoneId.systemDefault()),
                )
            }.orEmpty(),
            lastVisitedEpochMillis = workouts.firstOrNull()
                ?.session
                ?.startedAt
                ?.toEpochMilli()
                ?: 0L,
            workouts = workouts.map { it.toMapWorkout(settings) },
        )
    }.sortedWith(
        compareByDescending<TrainingPlaceUiModel>(TrainingPlaceUiModel::workoutCount)
            .thenByDescending(TrainingPlaceUiModel::lastVisitedEpochMillis),
    )
    val effectiveSelectedId = requestedPlaceId
        ?.takeIf { id -> places.any { it.id == id } }
        ?: places.firstOrNull()?.id
    return TrainingMapUiState(
        isLoading = false,
        period = period,
        weightUnit = settings.weightUnit,
        places = places,
        selectedPlaceId = effectiveSelectedId,
        workoutCount = locatedWorkouts.size,
        totalVolume = locatedWorkouts.sumOf(WorkoutDetails::totalVolumeGrams)
            .gramsIn(settings.weightUnit),
        totalDurationMinutes = locatedWorkouts.sumOf(WorkoutDetails::durationMinutes),
    )
}

private fun WorkoutDetails.toMapWorkout(settings: UserSettings): TrainingMapWorkoutUiModel =
    TrainingMapWorkoutUiModel(
        id = session.id.value,
        name = session.name,
        date = DATE_FORMATTER.format(
            session.startedAt.atZone(ZoneId.systemDefault()),
        ),
        completedSets = completedSetCount,
        volume = totalVolumeGrams.gramsIn(settings.weightUnit),
        durationMinutes = durationMinutes(),
    )

private fun WorkoutDetails.durationMinutes(): Long =
    session.finishedAt
        ?.let { Duration.between(session.startedAt, it).toMinutes().coerceAtLeast(0) }
        ?: 0

private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(
    "dd MMM yyyy, HH:mm",
    Locale.forLanguageTag("pt-BR"),
)
