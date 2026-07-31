package com.forgeflow.feature.exercises.presentation

import androidx.compose.runtime.Immutable
import com.forgeflow.core.model.Equipment
import com.forgeflow.core.model.ExerciseMediaType
import com.forgeflow.core.model.MuscleGroup
import com.forgeflow.core.model.PersonalRecordType
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.core.model.WorkoutSetType

@Immutable
data class ExerciseDetailsUiState(
    val isLoading: Boolean = true,
    val exercise: ExerciseDetailsUiModel? = null,
    val error: Boolean = false,
)

@Immutable
data class ExerciseDetailsUiModel(
    val id: String,
    val name: String,
    val muscleGroup: MuscleGroup,
    val secondaryMuscles: List<MuscleGroup>,
    val equipment: Equipment,
    val instructions: String,
    val mediaUri: String?,
    val mediaType: ExerciseMediaType?,
    val mediaThumbnailUri: String?,
    val weightUnit: WeightUnit,
    val sessionCount: Int,
    val completedSetCount: Int,
    val totalVolume: String,
    val maxWeight: String,
    val estimatedOneRepMax: String,
    val bestSet: String,
    val personalRecordCount: Int,
    val personalRecords: List<ExerciseRecordSummaryUiModel>,
    val chartPoints: List<ExerciseChartPointUiModel>,
    val sessions: List<ExerciseSessionUiModel>,
)

@Immutable
data class ExerciseChartPointUiModel(
    val value: Float,
    val label: String,
)

@Immutable
data class ExerciseSessionUiModel(
    val id: String,
    val workoutName: String,
    val date: String,
    val sets: List<ExerciseSessionSetUiModel>,
)

@Immutable
data class ExerciseSessionSetUiModel(
    val number: Int,
    val performance: String,
    val type: WorkoutSetType,
    val personalRecordTypes: Set<PersonalRecordType>,
)

val ExerciseSessionSetUiModel.isPersonalRecord: Boolean
    get() = personalRecordTypes.isNotEmpty()

@Immutable
data class ExercisePersonalRecordUiModel(
    val type: PersonalRecordType,
    val workoutName: String,
    val date: String,
    val performance: String,
)

@Immutable
data class ExerciseRecordSummaryUiModel(
    val type: PersonalRecordType,
    val current: ExercisePersonalRecordUiModel,
    val previous: List<ExercisePersonalRecordUiModel>,
)
