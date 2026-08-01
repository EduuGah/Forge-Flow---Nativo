package com.forgeflow.core.data.exercise

import com.forgeflow.core.common.result.AppError
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.common.time.AppClock
import com.forgeflow.core.database.exercise.asEntity
import com.forgeflow.core.database.exercise.asExternalModel
import com.forgeflow.core.model.Equipment
import com.forgeflow.core.model.Exercise
import com.forgeflow.core.model.ExerciseId
import com.forgeflow.core.model.MuscleGroup
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class DefaultExerciseRepository @Inject constructor(
    private val localDataSource: ExerciseLocalDataSource,
    private val clock: AppClock,
    private val catalogProvider: ExerciseCatalogProvider = ExerciseCatalogProvider.empty(),
    private val mediaStore: ExerciseMediaStore = ExerciseMediaStore.None,
) : ExerciseRepository {
    override fun observeExercises(): Flow<DataResult<List<Exercise>>> =
        localDataSource.observeExercises()
            .map { entities ->
                val result: DataResult<List<Exercise>> =
                    DataResult.Success(entities.map { it.asExternalModel() })
                result
            }
            .catch {
                emit(DataResult.Failure(AppError.LocalDataUnavailable))
            }

    override suspend fun seedDefaults(): DataResult<Int> = runCatching {
        val timestamp = clock.now()
        (
            ExerciseSeedData.create(timestamp) +
                catalogProvider.create(timestamp)
            )
            .map { it.asEntity() }
            .let { localDataSource.insertExercises(it) }
    }.fold(
        onSuccess = { insertedCount -> DataResult.Success(insertedCount) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    override suspend fun saveCustomExercise(
        id: ExerciseId?,
        name: String,
        muscleGroup: MuscleGroup,
        equipment: Equipment,
        instructions: String,
        sourceMediaUri: String?,
        removeMedia: Boolean,
    ): DataResult<ExerciseId> = runCatching {
        require(name.isNotBlank())
        val exerciseId = id ?: ExerciseId.create()
        val existing = localDataSource.getExercise(exerciseId.value)
        val now = clock.now()
        val media = when {
            removeMedia -> null
            sourceMediaUri != null -> mediaStore.importPhoto(exerciseId, sourceMediaUri)
            else -> existing?.asExternalModel()?.media
        }
        Exercise(
            id = exerciseId,
            name = name.trim(),
            primaryMuscleGroup = muscleGroup,
            secondaryMuscleGroups = emptySet(),
            equipment = equipment,
            instructions = instructions.trim(),
            media = media,
            isCustom = true,
            createdAt = existing?.createdAtEpochMillis?.let(java.time.Instant::ofEpochMilli) ?: now,
            updatedAt = now,
        ).asEntity().also { localDataSource.upsertExercise(it) }
        if (removeMedia) mediaStore.delete(exerciseId)
        exerciseId
    }.fold(
        onSuccess = { DataResult.Success(it) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    override suspend fun deleteCustomExercise(id: ExerciseId): DataResult<Unit> = runCatching {
        check(localDataSource.deleteCustomExercise(id.value))
        mediaStore.delete(id)
    }.fold(
        onSuccess = { DataResult.Success(Unit) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )
}
