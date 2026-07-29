package com.forgeflow.core.testing

import com.forgeflow.core.common.result.AppError
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.exercise.ExerciseRepository
import com.forgeflow.core.model.Exercise
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

    fun setExercises(exercises: List<Exercise>) {
        result.value = DataResult.Success(exercises)
    }

    fun emitLoadError() {
        result.value = DataResult.Failure(AppError.LocalDataUnavailable)
    }
}
