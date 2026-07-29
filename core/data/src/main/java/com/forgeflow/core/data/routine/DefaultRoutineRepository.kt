package com.forgeflow.core.data.routine

import com.forgeflow.core.common.result.AppError
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.common.time.AppClock
import com.forgeflow.core.database.routine.RoutineDao
import com.forgeflow.core.database.routine.RoutineEntity
import com.forgeflow.core.database.routine.RoutineExerciseEntity
import com.forgeflow.core.database.routine.asExternalModel
import com.forgeflow.core.model.RoutineDetails
import com.forgeflow.core.model.RoutineDraft
import com.forgeflow.core.model.RoutineExerciseId
import com.forgeflow.core.model.RoutineId
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class DefaultRoutineRepository @Inject constructor(
    private val routineDao: RoutineDao,
    private val clock: AppClock,
) : RoutineRepository {
    override fun observeRoutines(): Flow<DataResult<List<RoutineDetails>>> =
        routineDao.observeActive()
            .map { records ->
                DataResult.Success(records.map { it.asExternalModel() }) as DataResult<List<RoutineDetails>>
            }
            .catch {
                emit(DataResult.Failure(AppError.LocalDataUnavailable))
            }

    override suspend fun getRoutine(id: RoutineId): DataResult<RoutineDetails> = runCatching {
        requireNotNull(routineDao.getById(id.value)).asExternalModel()
    }.fold(
        onSuccess = { DataResult.Success(it) },
        onFailure = { DataResult.Failure(AppError.LocalDataUnavailable) },
    )

    override suspend fun saveRoutine(draft: RoutineDraft): DataResult<RoutineId> = runCatching {
        require(draft.name.isNotBlank())
        require(draft.exercises.isNotEmpty())
        val id = draft.id ?: RoutineId.create()
        val existing = draft.id?.let { routineDao.getById(it.value) }
        val now = clock.now().toEpochMilli()
        val routine = RoutineEntity(
            id = id.value,
            name = draft.name.trim(),
            description = draft.description.trim(),
            createdAtEpochMillis = existing?.routine?.createdAtEpochMillis ?: now,
            updatedAtEpochMillis = now,
            archivedAtEpochMillis = null,
        )
        val exercises = draft.exercises.mapIndexed { position, item ->
            RoutineExerciseEntity(
                id = RoutineExerciseId.create().value,
                routineId = id.value,
                exerciseId = item.exerciseId.value,
                position = position,
                notes = item.notes.trim(),
                defaultRestSeconds = item.restSeconds.coerceIn(0, 600),
                plannedRepetitionsMinimum = item.plannedRepetitions.minimum,
                plannedRepetitionsMaximum = item.plannedRepetitions.maximum,
                plannedSets = item.plannedSets.coerceIn(1, 12),
            )
        }
        routineDao.replace(routine, exercises)
        id
    }.fold(
        onSuccess = { DataResult.Success(it) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    override suspend fun archiveRoutine(id: RoutineId): DataResult<Unit> = runCatching {
        routineDao.archive(id.value, clock.now().toEpochMilli())
    }.fold(
        onSuccess = { DataResult.Success(Unit) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )
}
