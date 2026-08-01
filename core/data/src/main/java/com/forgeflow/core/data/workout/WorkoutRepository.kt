package com.forgeflow.core.data.workout

import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.model.Repetitions
import com.forgeflow.core.model.ExerciseId
import com.forgeflow.core.model.RoutineId
import com.forgeflow.core.model.SessionExerciseId
import com.forgeflow.core.model.Weight
import com.forgeflow.core.model.WorkoutDetails
import com.forgeflow.core.model.WorkoutLocation
import com.forgeflow.core.model.WorkoutSessionId
import com.forgeflow.core.model.WorkoutSetId
import com.forgeflow.core.model.WorkoutSetType
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun observeActiveWorkout(): Flow<DataResult<WorkoutDetails?>>

    fun observeHistory(): Flow<DataResult<List<WorkoutDetails>>>

    suspend fun startRoutine(routineId: RoutineId): DataResult<WorkoutSessionId>

    suspend fun updateSet(
        setId: WorkoutSetId,
        weight: Weight,
        repetitions: Repetitions,
        completed: Boolean,
    ): DataResult<Unit>

    suspend fun addSet(sessionExerciseId: SessionExerciseId): DataResult<Unit>

    suspend fun updateSetType(setId: WorkoutSetId, type: WorkoutSetType): DataResult<Unit>

    suspend fun deleteSet(setId: WorkoutSetId): DataResult<Unit>

    suspend fun updateExerciseNotes(
        sessionExerciseId: SessionExerciseId,
        notes: String,
    ): DataResult<Unit>

    suspend fun addExercise(
        sessionId: WorkoutSessionId,
        exerciseId: ExerciseId,
    ): DataResult<Unit>

    suspend fun replaceExercise(
        sessionExerciseId: SessionExerciseId,
        exerciseId: ExerciseId,
    ): DataResult<Unit>

    suspend fun deleteExercise(sessionExerciseId: SessionExerciseId): DataResult<Unit>

    suspend fun moveExerciseToPosition(
        sessionExerciseId: SessionExerciseId,
        targetPosition: Int,
    ): DataResult<Unit>

    suspend fun finishWorkout(
        sessionId: WorkoutSessionId,
        location: WorkoutLocation? = null,
    ): DataResult<Unit>

    suspend fun deleteCompletedWorkout(sessionId: WorkoutSessionId): DataResult<Unit>

    suspend fun discardActiveWorkout(): DataResult<Unit>
}
