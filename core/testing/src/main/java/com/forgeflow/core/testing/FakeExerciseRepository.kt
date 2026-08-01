package com.forgeflow.core.testing

import com.forgeflow.core.common.result.AppError
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.exercise.ExerciseRepository
import com.forgeflow.core.model.Equipment
import com.forgeflow.core.model.Exercise
import com.forgeflow.core.model.ExerciseId
import com.forgeflow.core.model.ExerciseMedia
import com.forgeflow.core.model.ExerciseMediaType
import com.forgeflow.core.model.MuscleGroup
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeExerciseRepository(
    exercises: List<Exercise> = emptyList(),
) : ExerciseRepository {
    private val result = MutableStateFlow<DataResult<List<Exercise>>>(
        DataResult.Success(exercises),
    )

    override fun observeExercises(): Flow<DataResult<List<Exercise>>> = result

    override suspend fun seedDefaults(): DataResult<Int> = DataResult.Success(0)

    override suspend fun saveCustomExercise(
        id: ExerciseId?,
        name: String,
        muscleGroup: MuscleGroup,
        equipment: Equipment,
        instructions: String,
        sourceMediaUri: String?,
        removeMedia: Boolean,
    ): DataResult<ExerciseId> {
        val exerciseId = id ?: ExerciseId.create()
        val current = (result.value as? DataResult.Success)?.value.orEmpty()
        val existing = current.firstOrNull { it.id == exerciseId }
        val exercise = Exercise(
            id = exerciseId,
            name = name,
            primaryMuscleGroup = muscleGroup,
            secondaryMuscleGroups = emptySet(),
            equipment = equipment,
            instructions = instructions,
            media = when {
                removeMedia -> null
                sourceMediaUri != null -> ExerciseMedia(
                    sourceMediaUri,
                    ExerciseMediaType.IMAGE,
                )
                else -> existing?.media
            },
            isCustom = true,
            createdAt = existing?.createdAt ?: Instant.EPOCH,
            updatedAt = Instant.EPOCH,
        )
        result.value = DataResult.Success(current.filterNot { it.id == exerciseId } + exercise)
        return DataResult.Success(exerciseId)
    }

    override suspend fun deleteCustomExercise(id: ExerciseId): DataResult<Unit> {
        val current = (result.value as? DataResult.Success)?.value.orEmpty()
        result.value = DataResult.Success(current.filterNot { it.id == id && it.isCustom })
        return DataResult.Success(Unit)
    }

    fun setExercises(exercises: List<Exercise>) {
        result.value = DataResult.Success(exercises)
    }

    fun emitLoadError() {
        result.value = DataResult.Failure(AppError.LocalDataUnavailable)
    }
}
