package com.forgeflow.core.data.exercise

import com.forgeflow.core.database.exercise.ExerciseEntity
import kotlinx.coroutines.flow.Flow

interface ExerciseLocalDataSource {
    fun observeExercises(): Flow<List<ExerciseEntity>>

    suspend fun insertExercises(exercises: List<ExerciseEntity>): Int

    suspend fun getExercise(id: String): ExerciseEntity?

    suspend fun upsertExercise(exercise: ExerciseEntity)

    suspend fun deleteCustomExercise(id: String): Boolean
}
