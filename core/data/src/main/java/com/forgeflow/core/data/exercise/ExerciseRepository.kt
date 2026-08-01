package com.forgeflow.core.data.exercise

import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.model.Equipment
import com.forgeflow.core.model.Exercise
import com.forgeflow.core.model.ExerciseId
import com.forgeflow.core.model.MuscleGroup
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun observeExercises(): Flow<DataResult<List<Exercise>>>

    suspend fun seedDefaults(): DataResult<Int>

    suspend fun saveCustomExercise(
        id: ExerciseId?,
        name: String,
        muscleGroup: MuscleGroup,
        equipment: Equipment,
        instructions: String,
        sourceMediaUri: String? = null,
        removeMedia: Boolean = false,
    ): DataResult<ExerciseId>

    suspend fun deleteCustomExercise(id: ExerciseId): DataResult<Unit>
}
